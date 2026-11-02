package com.madebyjoaogomes.portfolio.article;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findAllByOrderByCreatedAtDescIdDesc();

    List<Article> findByStatusOrderByPublishedOnDescIdDesc(PublicationStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
