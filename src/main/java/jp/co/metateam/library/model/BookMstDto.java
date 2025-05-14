package jp.co.metateam.library.model;

import java.security.Timestamp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 書籍マスタDTO
 */
@Getter
@Setter
public class BookMstDto {
    //BookMstDto クラスは、書籍情報を画面（HTMLフォームなど）とやり取りするためのDTO（データ転送オブジェクト） です。
    private Long id; 

    private String isbn;

    private String title;
    
    private Timestamp deletedAt;

    private BookMst bookMst;

}

