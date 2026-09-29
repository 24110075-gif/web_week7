package murach.data;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import murach.business.User;

public class UserDB {

    public static int insert(User user) {
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            if (emf == null) return 0;
            em = emf.createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            em.persist(user);
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error inserting user via JPA: " + e.getMessage());
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public static int update(User user) {
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            if (emf == null) return 0;
            em = emf.createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            em.merge(user);
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error updating user via JPA: " + e.getMessage());
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public static int delete(User user) {
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            if (emf == null) return 0;
            em = emf.createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            em.remove(em.merge(user));
            trans.commit();
            return 1;
        } catch (Exception e) {
            System.err.println("Error deleting user via JPA: " + e.getMessage());
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public static boolean emailExists(String email) {
        User u = selectUser(email);
        return u != null;
    }

    public static User selectUser(String email) {
        EntityManager em = null;
        try {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            if (emf == null) return null;
            em = emf.createEntityManager();
            String qString = "SELECT u FROM User u WHERE u.email = :email";
            TypedQuery<User> q = em.createQuery(qString, User.class);
            q.setParameter("email", email);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Error selecting user via JPA: " + e.getMessage());
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public static List<User> selectUsers() {
        EntityManager em = null;
        try {
            EntityManagerFactory emf = DBUtil.getEmFactory();
            if (emf == null) return new ArrayList<>();
            em = emf.createEntityManager();
            String qString = "SELECT u FROM User u ORDER BY u.lastName";
            TypedQuery<User> q = em.createQuery(qString, User.class);
            List<User> users = q.getResultList();
            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error selecting users via JPA: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
