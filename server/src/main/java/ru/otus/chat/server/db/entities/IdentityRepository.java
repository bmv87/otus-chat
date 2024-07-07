package ru.otus.chat.server.db.entities;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class IdentityRepository implements AutoCloseable {

    private static final String PERSISTENCE_UNIT_NAME = "IDENTITY";
    private static EntityManagerFactory factory;

    public IdentityRepository() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
    }

    public List<User> getUsersByRole(RoleEnum role) {
        var entityManager = factory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        cr.select(root).where(cb.equal(joinedRole.get("name"), role));
        List<User> users = entityManager.createQuery(cr).getResultList();

        entityManager.close();
        return users;
    }


    public void saveUser(User user) {
        var entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void saveRole(Role role) {
        var entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(role);
        entityManager.getTransaction().commit();
        entityManager.close();
    }


    public boolean isInRole(String username, RoleEnum role) {
        var entityManager = factory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        cr.select(root).where(cb.equal(cb.lower(root.get("username")), username.toLowerCase())).where(cb.equal(joinedRole.get("name"), role));
        User user = null;
        try {
            user = entityManager.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            System.out.printf("User not found by username %s and role %s %n", username, role);
        } finally {
            entityManager.close();
        }
        return user != null;
    }

    public User getUsersByUserName(String username) {
        var entityManager = factory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);

        cr.select(root).where(cb.equal(cb.lower(root.get("username")), username.toLowerCase()));
        User user = null;
        try {
            user = entityManager.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("User not found by username " + username);
        } finally {
            entityManager.close();
        }
        return user;
    }

    public User getUsersByLogin(String login) {
        var entityManager = factory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);

        cr.select(root).where(cb.equal(cb.lower(root.get("login")), login.toLowerCase()));
        User user = null;
        try {
            user = entityManager.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("User not found by login " + login);
        } finally {
            entityManager.close();
        }
        return user;
    }

    public UUID getRoleId(RoleEnum role) {
        var entityManager = factory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UUID> cr = cb.createQuery(UUID.class);
        Root<Role> root = cr.from(Role.class);

        cr.select(root.get("roleId")).where(cb.equal(root.get("name"), role));
        UUID id = null;
        try {
            id = entityManager.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Role not found by name " + role);
        } finally {
            entityManager.close();
        }
        return id;
    }

    @Override
    public void close() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }
}
