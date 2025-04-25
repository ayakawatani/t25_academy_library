package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
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
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra, Model model) {
          try {
    
                boolean errIsbnFlg = false;

                if(result.hasErrors()) {
                    model.addAttribute("bookMstDto", bookMstDto);
                    model.addAttribute("org.springframework.validation.BindingResult.bookMstDto",result);
                    return "book/add";
                }

                BookMst isbnExist = this.bookMstService.selectByIsbn(bookMstDto.getIsbn());
    
                if(isbnExist != null){
                    result.rejectValue("isbn", "error.value", "ISBNは登録済みです");
                    errIsbnFlg = true;
                    return "book/add";
                }
                
            
                bookMstService.save(bookMstDto);
    
                return "redirect:index";//urlに対してゲット通信

            } catch (Exception e) {
                log.error("登録失敗: " + e.getMessage());
                log.error("書籍情報の保存に失敗しました",e);
                model.addAttribute("errorMessage","書籍情報の保存中にエラーが発生しました。もう一度お試しください。");
        

    
                return "book/add";
            }      
    } 
}
    