package gr.hua.model.response;

import lombok.*;

import java.sql.Timestamp;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDocumentResponse {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Timestamp uploadedAt;
}
