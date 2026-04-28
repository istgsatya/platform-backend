package com.charityplatform.backend.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.charityplatform.backend.model.ContentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentPostRepository extends JpaRepository<ContentPost, Long> {

    @EntityGraph(value = "ContentPost.withCharity")
    List<ContentPost> findByCharityIdOrderByCreatedAtDesc(Long charityId);
}