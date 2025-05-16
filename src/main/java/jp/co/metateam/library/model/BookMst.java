package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 書籍マスタ
 */
@Entity
@Table(name = "BookMst") // @Table→対応するDBテーブル名を指定
public class BookMst {
    // BookMst は データベース構造をJavaで表現したクラス（JPAエンティティ→データベースのテーブルをJavaクラスで表現したもの）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** ISBN */
    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    /** 書籍タイトル */
    @Column(name = "title", nullable = false)
    private String title;

    /** 削除日時 */
    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    /** 削除フラグ */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    /** Getters */

    public Long getId() {
        return this.id;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public String getTitle() {
        return this.title;
    }

    public Timestamp getDeletedAt() {
        return this.deletedAt;
    }

     public boolean isDeleted() {
        return deleted;
    }

    /** Setters */

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
}
