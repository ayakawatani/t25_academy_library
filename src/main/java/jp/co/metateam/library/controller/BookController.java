package jp.co.metateam.library.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.DtoInstantiatingConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import jp.co.metateam.library.service.BookMstService.BookNotFoundException;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {

    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService) {
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();// BOOK_MSTの書籍データを取得

        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add";
    }

    @PostMapping("/book/add")
    public String createBook(@ModelAttribute("bookMstDto") BookMstDto bookMstDto, BindingResult result, Model model) {

        boolean checkResult = bookMstService.checkbook(bookMstDto, model);
        boolean checkIsbnResult = bookMstService.checkIsbnEntry(bookMstDto, model);

        // 画面変更します
        if (checkResult || checkIsbnResult) {
            return "book/add"; // バリデーションエラー時、登録画面に戻す
        }

        // 登録処理
        bookMstService.save(bookMstDto);
        return "redirect:/book/index"; // 正常登録後、一覧に戻る

    }

    @GetMapping("/book/edit") // ブラウザから「/book/edit」というURL（GETリクエスト）が来たときに、このメソッドを実行し編集画面に遷移する

    public String edit(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        // @RequestParam("id")：URLに付いてきた id パラメータ（例：/book/edit?id=3）を受け取って、idを変数に入れる
        // Model：画面（HTML）に渡すデータを入れる箱
        // RedirectAttributes：リダイレクト先に一時的にデータを渡したいときに使う特別なデータの箱

        if (!model.containsAttribute("bookMstDto")) {
            // 画面に渡すデータ bookMstDto がまだ model に入っていないか確認
            // バリデーションエラーなどで一度リダイレクトされた後、すでにデータが入っているかもしれないので、それを防ぐためのチェック
            // エラーメッセージ付きで編集画面に戻ってきた場合、もうすでに model に bookMstDto が入ってるから実行されない！

            try {
                BookMst book = bookMstService.findById(id);
                // id を使って、書籍のデータをデータベースから取得
                // もしデータがなければ、RuntimeException などが投げられる前提

                BookMstDto dto = new BookMstDto();
                // 編集用のデータ転送オブジェクト（DTO）を作ります。画面に表示するデータの入れ物

                dto.setId(book.getId());//bookからIDをもらって、そのIDをdtoに入れてる
                dto.setTitle(book.getTitle());
                dto.setIsbn(book.getIsbn());
                //データベースから取ってきた BookMst の情報を BookMstDto にコピー

                model.addAttribute("bookMstDto", dto);
                 //DTOをmodelに追加します。これでHTML画面で${bookMstDto}として使えるようになる modelは画面に渡すための箱
                //ビュー側で bookMstDto という名前で dto のデータにアクセスできるようにする
                //addAttribute() メソッドは 最初の引数 に名前を渡し、2番目の引数 にデータを渡す必要がある

            } catch (BookNotFoundException e) {
                redirectAttributes.addFlashAttribute("popupMessage", "この書籍は既に削除されています");
                return "redirect:/book/index";
            }
        }

        return "book/edit";// 編集画面に遷移
    }

    @PostMapping("/book/edit")
    public String updateBook(
            @ModelAttribute("bookMstDto") BookMstDto bookMstDto, // 画面から送られてきたフォームのデータを、BookMstDto オブジェクトとして受け取る
            // BindingResult result,//@ModelAttribute でデータをバインドした後、エラーがあったかどうかをチェックするために使う
            Model model, // ビューに渡すデータを格納するためのオブジェクト
            RedirectAttributes redirectAttributes) {

        boolean success;
        try {
            success = bookMstService.updateBook(bookMstDto, model);
        } catch (BookNotFoundException e) {
            // 書籍が見つからなかった場合（idが削除されている）
            redirectAttributes.addFlashAttribute("popupMessage", "この書籍は既に削除されています");
            return "redirect:/book/index";
        }

        if (!success) {
            return "book/edit";// success が false の場合（更新が失敗した場合）、書籍編集画面に戻る
        }

        return "redirect:/book/index";
    }
}
