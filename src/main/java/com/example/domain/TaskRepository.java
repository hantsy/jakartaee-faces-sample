package com.example.domain;

import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author hantsy
 */
@Stateless
public class TaskRepository {

    @PersistenceContext
    EntityManager em;

    public Task findById(Long id) {
        Task task = em.find(Task.class, id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }

    public Optional<Task> findOptionalById(Long id) {
        Task task = em.find(Task.class, id);
        return Optional.ofNullable(task);
    }

    public List<Task> findByStatus(Task.Status status) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<Task> q = cb.createQuery(Task.class);
        Root<Task> c = q.from(Task.class);
        
        if (null != status) {
            q.where(cb.equal(c.get(Task_.status), status));
        }

        TypedQuery<Task> query = em.createQuery(q);

        return query.getResultList();
    }

    public Task save(Task task) {
        em.persist(task);

        return task;
    }

    public Task update(Task task) {
        return em.merge(task);
    }

    public void delete(Task task) {
        task = em.merge(task);
        em.remove(task);
    }

    public void deleteById(Long id) {
        Task task = this.findById(id);
        em.remove(task);
    }

}
