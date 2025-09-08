package com.equip.equiprental.filestorage.domain;

import com.equip.equiprental.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="file_Tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="file_id")
    private Long fileId;

    @Column(name="related_type")
    private String relatedType;

    @Column(name="related_id")
    private Long relatedId;

    @Column(name="original_name")
    private String originalName;

    @Column(name="unique_name")
    private String uniqueName;

    @Column(name="file_order")
    private Integer fileOrder;

    @Column(name="file_type")
    private String fileType;

    @Column(name="file_path")
    private String filePath;

    @Column(name="file_size")
    private Long fileSize;
}
