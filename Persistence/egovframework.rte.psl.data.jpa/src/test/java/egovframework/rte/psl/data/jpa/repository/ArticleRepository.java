package egovframework.rte.psl.data.jpa.repository;

import egovframework.rte.psl.data.jpa.domain.Article;

import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article, Long> {
	
}
