package com.madebyjoaogomes.portfolio.project;

import com.madebyjoaogomes.portfolio.content.PublicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByDisplayOrderAscIdAsc();

    List<Project> findByStatusOrderByDisplayOrderAscIdAsc(PublicationStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("select coalesce(max(p.displayOrder), 0) from Project p")
    int findMaxDisplayOrder();
}
