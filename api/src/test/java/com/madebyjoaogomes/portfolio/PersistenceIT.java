package com.madebyjoaogomes.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.madebyjoaogomes.portfolio.article.ArticleRepository;
import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import com.madebyjoaogomes.portfolio.project.ProjectRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Runs the Flyway migrations on a real PostgreSQL and checks the mappings against them. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PersistenceIT {

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private ArticleRepository articles;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesProjectBlocksAsJsonb() {
        var saved = projects.saveAndFlush(TestData.project("cm-advocacia", 1));
        entityManager.clear();

        var loaded = projects.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getDetails()).isEqualTo(TestData.details());
        assertThat(loaded.getSeo()).isEqualTo(TestData.seo());
        assertThat(loaded.getCreatedAt()).isNotNull();

        var featureTitle = jdbc.queryForObject(
                "select details -> 'features' -> 0 ->> 'title' from projects where id = ?", String.class, saved.getId());
        assertThat(featureTitle).isEqualTo("Feature");
    }

    @Test
    void storesArticleTagsAsTextArray() {
        var saved = articles.saveAndFlush(TestData.publishedArticle("primeiro-artigo", LocalDate.of(2026, 11, 2)));
        entityManager.clear();

        var loaded = articles.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getTags()).containsExactly("Java", "Spring");
        assertThat(loaded.getStatus()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(loaded.getPublishedOn()).isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    void rejectsDuplicateSlug() {
        projects.saveAndFlush(TestData.project("gelatto", 1));

        assertThatThrownBy(() -> projects.saveAndFlush(TestData.project("gelatto", 2)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMalformedSlug() {
        assertThatThrownBy(() -> articles.saveAndFlush(TestData.article("Não é Slug")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsPublishedArticleWithoutDate() {
        articles.saveAndFlush(TestData.article("rascunho"));

        assertThatThrownBy(() -> jdbc.update("update articles set status = 'PUBLISHED' where slug = 'rascunho'"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
