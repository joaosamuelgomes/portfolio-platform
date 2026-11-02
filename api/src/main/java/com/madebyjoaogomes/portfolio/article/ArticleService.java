package com.madebyjoaogomes.portfolio.article;

import com.madebyjoaogomes.portfolio.content.ContentNotFoundException;
import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import com.madebyjoaogomes.portfolio.content.SlugConflictException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articles;
    private final Clock clock;

    public ArticleService(ArticleRepository articles, Clock clock) {
        this.articles = articles;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> list(PublicationStatus status) {
        var found = status == null
                ? articles.findAllByOrderByCreatedAtDescIdDesc()
                : articles.findByStatusOrderByPublishedOnDescIdDesc(status);
        return found.stream().map(ArticleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse get(Long id) {
        return ArticleResponse.from(find(id));
    }

    public ArticleResponse create(ArticleRequest request) {
        if (articles.existsBySlug(request.slug())) {
            throw new SlugConflictException(request.slug());
        }
        var article = new Article();
        apply(article, request);
        return ArticleResponse.from(articles.saveAndFlush(article));
    }

    public ArticleResponse update(Long id, ArticleRequest request) {
        var article = find(id);
        if (articles.existsBySlugAndIdNot(request.slug(), id)) {
            throw new SlugConflictException(request.slug());
        }
        apply(article, request);
        if (article.getStatus() == PublicationStatus.PUBLISHED && article.getPublishedOn() == null) {
            // A published article always carries a date; clearing it re-stamps today.
            article.publish(LocalDate.now(clock));
        }
        return ArticleResponse.from(articles.saveAndFlush(article));
    }

    public void delete(Long id) {
        articles.delete(find(id));
    }

    public ArticleResponse publish(Long id) {
        var article = find(id);
        article.publish(LocalDate.now(clock));
        return ArticleResponse.from(articles.saveAndFlush(article));
    }

    public ArticleResponse unpublish(Long id) {
        var article = find(id);
        article.unpublish();
        return ArticleResponse.from(articles.saveAndFlush(article));
    }

    public ArticleResponse archive(Long id) {
        var article = find(id);
        article.archive();
        return ArticleResponse.from(articles.saveAndFlush(article));
    }

    private Article find(Long id) {
        return articles.findById(id).orElseThrow(() -> new ContentNotFoundException("Artigo", id));
    }

    private static void apply(Article article, ArticleRequest request) {
        article.update(request.slug(), request.title(), request.description(), request.markdown(), request.tags(),
                request.image(), request.imageWidth(), request.imageHeight(), request.imageAlt(),
                request.publishedAt(), request.updatedAt());
    }
}
