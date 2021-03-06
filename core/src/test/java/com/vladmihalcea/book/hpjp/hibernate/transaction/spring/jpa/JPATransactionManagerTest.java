package com.vladmihalcea.book.hpjp.hibernate.transaction.spring.jpa;

import com.vladmihalcea.book.hpjp.hibernate.transaction.forum.Post;
import com.vladmihalcea.book.hpjp.hibernate.transaction.forum.Tag;
import com.vladmihalcea.book.hpjp.hibernate.transaction.spring.jpa.config.JPATransactionManagerConfiguration;
import com.vladmihalcea.book.hpjp.hibernate.transaction.spring.jpa.dao.PostBatchDAO;
import com.vladmihalcea.book.hpjp.hibernate.transaction.spring.jpa.service.ForumService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vlad Mihalcea
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JPATransactionManagerConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JPATransactionManagerTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ForumService forumService;

    @Autowired
    private PostBatchDAO postBatchDAO;

    @Test
    public void test() {
        try {
            transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                Tag hibernate = new Tag();
                hibernate.setName("hibernate");
                entityManager.persist(hibernate);

                Tag jpa = new Tag();
                jpa.setName("jpa");
                entityManager.persist(jpa);
                return null;
            });
        } catch (TransactionException e) {
            LOGGER.error("Failure", e);
        }

        try {
            transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                postBatchDAO.savePosts();
                return null;
            });
        } catch (TransactionException e) {
            LOGGER.error("Failure", e);
        }

        Post post = forumService.newPost("High-Performance Java Persistence", "hibernate", "jpa");
        assertNotNull(post.getId());
    }
}
