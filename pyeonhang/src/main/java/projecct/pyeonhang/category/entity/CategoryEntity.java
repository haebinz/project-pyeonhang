package projecct.pyeonhang.category.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryId;

    private String categoryCode;

    private String categoryName;

    @Column( columnDefinition = "CHAR(1)")
    private String useYn;
}
