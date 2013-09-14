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
    private static Boolean cachable = Boolean.valueOf(true);
    private Session session;
    private Order order = Order.asc("id");
    public static final int ASC = 0;
    public static final int DESC = 1;
    Map<String, Object> criteriaEQ;
    Map<String, Object> criteriaNE;
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

    public Model addCriteria(String key, Object value) {
        return addCriteria(key, value, Boolean.valueOf(true));
    }

    public Model addCriteria(String key, Object value, Boolean eq) {
        if (eq.booleanValue()) {
            if (this.criteriaEQ == null) {
                this.criteriaEQ = new HashMap();
            }
            this.criteriaEQ.put(key, value);
        } else {
            if (this.criteriaNE == null) {
                this.criteriaNE = new HashMap();
            }
            this.criteriaNE.put(key, value);
        }
        return this;
    }

    public Model addCriteria(Map params) {
        if (this.criteriaEQ == null) {
            this.criteriaEQ = new HashMap();
        }
        this.criteriaEQ.putAll(params);
        return this;
    }

    public List find() {
        return findAll();
    }

    public List findAll() {
        List result = createCriteria().list();
        this.session.close();
        return result;
    }

    public Object findOne() {
        Object result = createCriteria().uniqueResult();
        this.session.close();
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

        this.session.close();
        return result;
    }

    public List find(Map<String, String> params) {
        return findByCriteria(params);
    }

    public List findByCriteria(Map<String, String> params) {
        this.criteriaEQ.putAll(params);
        return findAll();
    }

    public void remove(Object object) {
        this.session = HibernateUtil.getSession();
        this.session.beginTransaction();
        try {
            this.session.delete(object);
            this.session.getTransaction().commit();
        } catch (Exception e) {
            if (e.getCause() != null) {
                _log.error(e.getCause().toString());
            } else {
                _log.error(e.toString());
            }
            this.session.getTransaction().rollback();
        } finally {
            this.session.close();
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
        this.session.close();
        return count;
    }

    public void save(Object object) {
        this.session = HibernateUtil.getSession();
        this.session.beginTransaction();
        try {
            this.session.saveOrUpdate(object);
            this.session.getTransaction().commit();
        } catch (Exception e) {
            if (e.getCause() != null) {
                _log.error(e.getCause().toString());
            } else {
                _log.error(e.toString());
            }
            this.session.getTransaction().rollback();
        } finally {
            this.session.close();
        }
    }

    private Criteria createCriteria() {
        this.session = HibernateUtil.getSession();

        Criteria criteria = this.session.createCriteria(this.modelClass).setCacheable(cachable.booleanValue());
        if (this.limit != null) {
            criteria.setMaxResults(this.limit.intValue());
        }
        if (this.offset != null) {
            criteria.setFirstResult(this.offset.intValue());
        }
        if (this.order != null) {
            criteria.addOrder(this.order);
        }
        if (this.criteriaEQ != null) {
            for (String key : this.criteriaEQ.keySet()) {
                criteria.add(Restrictions.eq(key, this.criteriaEQ.get(key)));
            }
        }
        if (this.criteriaNE != null) {
            for (String key : this.criteriaNE.keySet()) {
                criteria.add(Restrictions.ne(key, this.criteriaNE.get(key)));
            }
        }
        clear();
        return criteria;
    }

    private void clear() {
        this.criteriaEQ = null;
        this.criteriaNE = null;
        this.limit = null;
        this.offset = null;
        this.order = null;
    }
}