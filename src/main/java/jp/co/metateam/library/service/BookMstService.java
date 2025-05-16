package jp.co.metateam.library.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.BookMstRepository;

import java.sql.Timestamp;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;

    @Autowired
    public BookMstService(BookMstRepository bookMstRepository) {
        this.bookMstRepository = bookMstRepository;
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);// booksはリスト
            BookMstDto bookMstDto = new BookMstDto();// BookMstDtoにデータを詰めなおす
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // AccountDtoからAccountへの変換
            BookMst bookMst = new BookMst();

            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());
            // bookMst.setId(bookMstDto.getId());
            // bookMst.setDeletedAt(this.bookMstRepository.encode(bookMstDto.getDeletedAt()));
            // // パスワードをハッシュ化してから保存
            // bookMst.setEmail(bookMstDto.getEmail());

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping

    // バリデーションチェック
    public boolean checkbook(BookMstDto bookMstDto, Model model) {
        String Title = bookMstDto.getTitle();
        List<String> validationTitleErrors = new ArrayList<String>();

        // 1. 書籍名のバリデーションチェックをする
        if (StringUtils.isEmpty(Title)) {
            validationTitleErrors.add("書籍名は必須です。");
            model.addAttribute("titleErrors", validationTitleErrors);

        } else if (Title.length() > 255) {
            validationTitleErrors.add("書籍名は255文字以内で入力してください");
            model.addAttribute("titleErrors", validationTitleErrors);
        }

        if (!validationTitleErrors.isEmpty()) {
            return true;
        }
        return false;
    }

    public Boolean checkIsbnEntry(BookMstDto bookMstDto, Model model) {

        // String getIsbn = bookMstDto.getIsbn();
        // List<String> errIsbnList = new ArrayList<>();

        String isbn = bookMstDto.getIsbn();
        List<String> validationIsbnErrors = new ArrayList<String>();

        // 2. ISBNのバリデーションチェック
        if (StringUtils.isEmpty(isbn)) {
            validationIsbnErrors.add("ISBNは必須です");
            model.addAttribute("isbnErrors", validationIsbnErrors);
            return true;
        }

        if (isbn.length() != 13) {
            validationIsbnErrors.add("ISBNは13文字で入力してください");
            model.addAttribute("isbnErrors", validationIsbnErrors);
        }

        if (!isbn.matches("^[0-9]+$")) {
            validationIsbnErrors.add("ISBNは半角数字で入力してください");
            model.addAttribute("isbnErrors", validationIsbnErrors);
            return true;
        }

        List<BookMst> bookMst = this.bookMstRepository.selectByIsbn(isbn);// ISBNの重複チェック

        if (!bookMst.isEmpty()) {
            validationIsbnErrors.add("登録済みのISBNです");
            model.addAttribute("isbnErrors", validationIsbnErrors);
        }

        if (!validationIsbnErrors.isEmpty()) {
            return true;
        }
        return false;
    }

    public class BookNotFoundException extends RuntimeException {
        public BookNotFoundException(String message) {
            super(message);
        }
    }

    public BookMst findById(Long id) {
        // ① IDが見つからなかったらエラー
        BookMst book = bookMstRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません: id=" + id));

        // ② IDはあるけど deleted = true（論理削除済み）だったらエラー
        if (book.isDeleted()) {
            throw new BookNotFoundException("この書籍は削除されています: id=" + id);
        }

        return book;
    }

    @Transactional
    public boolean updateBook(BookMstDto bookMstDto, Model model) {
        // bookMstDto：画面から来た新しい本のデータ
        // model：エラーがあったときにメッセージを画面に渡すためのデータの箱
        // boolean：成功なら true、失敗なら false を返す

        BookMst existingBook = findById(bookMstDto.getId());
        // 本のIDを使って、今データベースにある元の本のデータを取り出す

        boolean isTitleChanged = !bookMstDto.getTitle().equals(existingBook.getTitle());
        // 「タイトルが変わったかどうか？」を調べてる

        boolean isIsbnChanged = !bookMstDto.getIsbn().equals(existingBook.getIsbn());
        // 「ISBNが変わったかどうか？」を調べてる

        // 変更がない場合
        if (!isTitleChanged && !isIsbnChanged) {
            model.addAttribute("errorMessage", "変更点がありません");
            return false;
        }

        boolean hasTitleError = false;// 最初はエラーはないと仮定
        boolean hasIsbnError = false;

        // もしタイトルが変わってたら、エラーがあるか調べる
        if (isTitleChanged) {
            hasTitleError = checkbook(bookMstDto, model);
        }

        // もしISBNが変わってたら、エラーがあるか調べる
        if (isIsbnChanged) {
            hasIsbnError = checkIsbnEntry(bookMstDto, model);
        }

        // どっちかでもエラーがあったら、更新せずに終わる
        if (hasTitleError || hasIsbnError) {
            return false;
        }

        // 正常更新
        existingBook.setTitle(bookMstDto.getTitle());
        existingBook.setIsbn(bookMstDto.getIsbn());
        bookMstRepository.save(existingBook);

        return true;
    }

    @Transactional
    public void delete(Long id) {
        BookMst book = bookMstRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません"));

        // すでに削除されてたらエラー（もう削除済みです！）
        if (book.isDeleted()) {
            throw new BookNotFoundException("この書籍は既に削除されています");
        }

        book.setDeleted(true); // 削除フラグを true に
        book.setDeletedAt(Timestamp.from(Instant.now())); // ★ 今の時間をセット！
        bookMstRepository.save(book); // データベースに保存
    }

}
