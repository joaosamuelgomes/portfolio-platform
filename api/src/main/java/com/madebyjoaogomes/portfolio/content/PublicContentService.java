package com.madebyjoaogomes.portfolio.content;

import com.madebyjoaogomes.portfolio.article.ArticleRepository;
import com.madebyjoaogomes.portfolio.project.ProjectRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicContentService {

    private final ProjectRepository projects;
    private final ArticleRepository articles;
    private final Clock clock;

    public PublicContentService(ProjectRepository projects, ArticleRepository articles, Clock clock) {
        this.projects = projects;
        this.articles = articles;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ContentSnapshot snapshot() {
        return new ContentSnapshot(
                ContentSnapshot.SCHEMA_VERSION,
                clock.instant(),
                projects.findByStatusOrderByDisplayOrderAscIdAsc(PublicationStatus.PUBLISHED).stream()
                        .map(PublishedProject::from)
                        .toList(),
                articles.findByStatusOrderByPublishedOnDescIdDesc(PublicationStatus.PUBLISHED).stream()
                        .map(PublishedArticle::from)
                        .toList());
    }
}
