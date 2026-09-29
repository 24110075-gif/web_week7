package murach.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import murach.business.User;

public class UserDB {

    public static int insert(User user) {
        EntityManager em = DBUtil.getEmFactory().createEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        try {
            em.persist(user);
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error inserting user via JPA: " + e.getMessage());
            if (trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            em.close();
        }
    }

    public static int update(User user) {
        EntityManager em = DBUtil.getEmFactory().createEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        try {
            em.merge(user);
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error updating user via JPA: " + e.getMessage());
            if (trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            em.close();
        }
    }

    public static int delete(User user) {
        EntityManager em = DBUtil.getEmFactory().createEntityManager();
        EntityTransaction trans = em.getTransaction();
        trans.begin();
        try {
            em.remove(em.merge(user));
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error deleting user via JPA: " + e.getMessage());
            if (trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            em.close();
        }
    }

    public static boolean emailExists(String email) {
        User u = selectUser(email);
        return u != null;
    }

    public static User selectUser(String email) {
        EntityManager em = DBUtil.getEmFactory().createEntityManager();
        String qString = "SELECT u FROM User u WHERE u.email = :email";
        TypedQuery<User> q = em.createQuery(qString, User.class);
        q.setParameter("email", email);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public static List<User> selectUsers() {
        EntityManager em = DBUtil.getEmFactory().createEntityManager();
        String qString = "SELECT u FROM User u ORDER BY u.lastName";
        TypedQuery<User> q = em.createQuery(qString, User.class);
        List<User> users;
        try {
            users = q.getResultList();
            if (users == null) {
                users = new ArrayList<>();
            }
            return users;
        } catch (Exception e) {
            System.err.println("Error selecting users via JPA: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
