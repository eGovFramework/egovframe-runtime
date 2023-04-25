package org.egovframe.rte.psl.data.jpa.repository;

import org.egovframe.rte.psl.data.jpa.domain.Article;
import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article, Long> {
	
}
