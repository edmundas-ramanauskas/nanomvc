package com.nanomvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model {

    private static Logger _log = LoggerFactory.getLogger(Model.class);
    private static Boolean cachable = true;
    private Session session;
    private Order order = Order.asc("id");
    public static final int ASC = 0;
    public static final int DESC = 1;
    private Map<String, Object> criteriaEQ;
    private Map<String, Object> criteriaNE;
    private Map<String, String> alias;
    private Integer limit;
    private Integer offset;
    private Class modelClass;

    public Model() {
    }

    public Model(Class modelClass) {
        this.modelClass = modelClass;
    }

    public Model setOrder(String field, int order) {
        if (order == 1) {
            this.order = Order.desc(field);
        } else {
            this.order = Order.asc(field);
        }
        return this;
    }

    public Model setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Model setLimit(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }
    
    public Model addAlias(String name, String alias) {
        if(this.alias == null)
            this.alias = new HashMap<>();
        this.alias.put(name, alias);
        return this;
    }

    public Model addCriteria(String key, Object value) {
        return addCriteria(key, value, Boolean.valueOf(true));
    }

    public Model addCriteria(String key, Object value, Boolean eq) {
        if (eq.booleanValue()) {
            if (criteriaEQ == null) {
                criteriaEQ = new HashMap();
            }
            criteriaEQ.put(key, value);
        } else {
            if (criteriaNE == null) {
                criteriaNE = new HashMap();
            }
            criteriaNE.put(key, value);
        }
        return this;
    }

    public Model addCriteria(Map params) {
        if (criteriaEQ == null) {
            criteriaEQ = new HashMap();
        }
        criteriaEQ.putAll(params);
        return this;
    }

    public List find() {
        return findAll();
    }

    public List findAll() {
        List result = createCriteria().list();
        session.close();
        return result;
    }

    public Object findOne() {
        Object result = createCriteria().uniqueResult();
        session.close();
        return result;
    }

    public Object find(Object id) {
        return findByPk(id);
    }

    public Object findById(Object id) {
        return findByPk(id);
    }

    public Object findByPk(Object id) {
        Object result = createCriteria().add(Restrictions.idEq(id)).uniqueResult();
        
        session.close();
        return result;
    }
    
    public List findBetween(String field, Object start, Object end) {
        List result = createCriteria()
                    .add(Restrictions.between(field, start, end)).list();
        session.close();
        return result;
    }
    
    public List findIn(String field, List in) {
        List result = createCriteria()
                    .add(Restrictions.in(field, in)).list();
        session.close();
        return result;
    }
    
    public Long countBetween(String field, Object start, Object end) {
        Long count = (Long) createCriteria()
                    .add(Restrictions.between(field, start, end))
                    .setProjection(Projections.rowCount()).uniqueResult();
        session.close();
        return count;
    }
    
    public Long countIn(String field, List in) {
        Long count = (Long) createCriteria()
                    .add(Restrictions.in(field, in))
                    .setProjection(Projections.rowCount()).uniqueResult();
        session.close();
        return count;
    }

    public List find(Map<String, String> params) {
        return findByCriteria(params);
    }

    public List findByCriteria(Map<String, String> params) {
        criteriaEQ.putAll(params);
        return findAll();
    }

    public void remove(Object object) {
        session = HibernateUtil.getSession();
        session.beginTransaction();
        try {
            session.delete(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (e.getCause() != null) {
                _log.error(e.getCause().toString());
            } else {
                _log.error(e.toString());
            }
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }

    public void removeByPk(Object id) {
        remove(findByPk(id));
    }

    public Long count() {
        return countAll();
    }

    public Long countAll() {
        Long count = (Long) createCriteria().setProjection(Projections.rowCount()).uniqueResult();
        session.close();
        return count;
    }

    public void save(Object object) {
        session = HibernateUtil.getSession();
        session.beginTransaction();
        try {
            session.saveOrUpdate(object);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (e.getCause() != null) {
                _log.error(e.getCause().toString());
            } else {
                _log.error(e.toString());
            }
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }

    private Criteria createCriteria() {
        session = HibernateUtil.getSession();

        Criteria criteria = session.createCriteria(modelClass).setCacheable(cachable);
        if (limit != null) {
            criteria.setMaxResults(limit);
        }
        if (offset != null) {
            criteria.setFirstResult(offset);
        }
        if (order != null) {
            criteria.addOrder(order);
        }
        if (criteriaEQ != null) {
            for (String key : criteriaEQ.keySet()) {
                criteria.add(Restrictions.eq(key, criteriaEQ.get(key)));
            }
        }
        if (criteriaNE != null) {
            for (String key : criteriaNE.keySet()) {
                criteria.add(Restrictions.ne(key, criteriaNE.get(key)));
            }
        }
        if(alias != null) {
            for (String name : alias.keySet()) {
                criteria.createAlias(name, alias.get(name));
            }
        }
        clear();
        return criteria;
    }

    private void clear() {
        criteriaEQ = null;
        criteriaNE = null;
        limit = null;
        offset = null;
        order = null;
    }
}