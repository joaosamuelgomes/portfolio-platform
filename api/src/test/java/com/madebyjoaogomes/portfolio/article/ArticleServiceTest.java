package com.madebyjoaogomes.portfolio.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.madebyjoaogomes.portfolio.TestData;
import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import com.madebyjoaogomes.portfolio.content.SlugConflictException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    // 02:00 UTC is still the previous day in São Paulo, which is the date an editor expects.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T02:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);

    @Mock
    private ArticleRepository articles;

    private ArticleService service;

    @BeforeEach
    void setUp() {
        service = new ArticleService(articles, CLOCK);
        lenient().when(articles.saveAndFlush(any())).then(returnsFirstArg());
    }

    @Test
    void publishingUndatedArticleStampsTodayInTheEditorsZone() {
        when(articles.findById(1L)).thenReturn(Optional.of(TestData.article("rascunho")));

        var published = service.publish(1L);

        assertThat(published.status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(published.publishedAt()).isEqualTo(TODAY);
    }

    @Test
    void publishingKeepsAnExistingDate() {
        var article = TestData.article("agendado");
        article.update("agendado", "Título", "Descrição", "Texto", List.of(), null, null, null, null,
                LocalDate.of(2026, 1, 15), null);
        when(articles.findById(1L)).thenReturn(Optional.of(article));

        assertThat(service.publish(1L).publishedAt()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void publishedArticleNeverLosesItsDate() {
        when(articles.findById(1L)).thenReturn(Optional.of(TestData.publishedArticle("publicado", LocalDate.of(2026, 5, 1))));

        var updated = service.update(1L, request("publicado", null));

        assertThat(updated.status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(updated.publishedAt()).isEqualTo(TODAY);
    }

    @Test
    void unpublishingKeepsTheDate() {
        when(articles.findById(1L)).thenReturn(Optional.of(TestData.publishedArticle("publicado", LocalDate.of(2026, 5, 1))));

        var draft = service.unpublish(1L);

        assertThat(draft.status()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(draft.publishedAt()).isEqualTo(LocalDate.of(2026, 5, 1));
    }

    @Test
    void rejectsSlugAlreadyInUse() {
        when(articles.existsBySlug("repetido")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("repetido", null))).isInstanceOf(SlugConflictException.class);
    }

    @Test
    void createsDraftWithEmptyTagsByDefault() {
        when(articles.existsBySlug("novo")).thenReturn(false);

        var created = service.create(request("novo", null));

        assertThat(created.status()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(created.tags()).isEmpty();
    }

    private static ArticleRequest request(String slug, LocalDate publishedAt) {
        return new ArticleRequest(slug, "Título", "Descrição", "## Seção\n\nTexto", null,
                null, null, null, null, publishedAt, null);
    }
}
