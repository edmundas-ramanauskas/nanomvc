package org.nanomvc.mvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.nanomvc.utils.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model {

    private static final Logger _log = LoggerFactory.getLogger(Model.class);
    private static final Boolean cachable = true;
    private Session session;
    private Order order = Order.asc("id");
    public static final int ASC = 0;
    public static final int DESC = 1;
    private Map<String, Restriction> criteria;
    private Map<String, String> alias;
    private Map<String, Between> between;
    private Integer limit;
    private Integer offset;
    private final Class modelClass;

    public Model(Class modelClass) {
        this.modelClass = modelClass;
    }

    public void setOrder(String field, int order) {
        if (order == 1) {
            this.order = Order.desc(field);
        } else {
            this.order = Order.asc(field);
        }
    }

    public Model order(String field, int order) {
        if (order == 1) {
            this.order = Order.desc(field);
        } else {
            this.order = Order.asc(field);
        }
        return this;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setLimit(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public Model limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Model limit(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }
    
    public void addAlias(String name, String alias) {
        if(this.alias == null)
            this.alias = new HashMap<>();
        this.alias.put(name, alias);
    }
    
    public Model alias(String name, String alias) {
        if(this.alias == null)
            this.alias = new HashMap<>();
        this.alias.put(name, alias);
        return this;
    }

    public void addCriteria(String key, Object value, Comparator comp) {
        if (criteria == null) {
            criteria = new HashMap();
        }
        criteria.put(key, new Restriction(value, comp));
    }

    public Model criteria(String key, Object value, Comparator comp) {
        addCriteria(key, value, comp);
        return this;
    }

    public void addCriteria(String key, Object value) {
        addCriteria(key, value, Comparator.EQ);
    }

    public Model criteria(String key, Object value) {
        addCriteria(key, value, Comparator.EQ);
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
    
    public Object findOne(String name, Object value) {
        Object result = createCriteria().add(Restrictions.eq(name, value))
                .uniqueResult();
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
    
    public List findBetween(String field, Object min, Object max) {
        List result = createCriteria()
                    .add(Restrictions.between(field, min, max)).list();
        session.close();
        return result;
    }
    
    public List findIn(String field, List in) {
        List result = createCriteria()
                    .add(Restrictions.in(field, in)).list();
        session.close();
        return result;
    }
    
    public Long countBetween(String field, Object min, Object max) {
        Long count = (Long) createCriteria()
                    .add(Restrictions.between(field, min, max))
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
        Long count = (Long) createCriteria().setProjection(Projections.rowCount())
                .uniqueResult();
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
        Criteria criteria = session.createCriteria(modelClass)
                .setCacheable(cachable);
        if (limit != null) {
            criteria.setMaxResults(limit);
        }
        if (offset != null) {
            criteria.setFirstResult(offset);
        }
        if (order != null) {
            criteria.addOrder(order);
        }
        if (this.criteria != null) {
            for (String key : this.criteria.keySet()) {
                Restriction restriction = this.criteria.get(key);
                switch(restriction.getType()) {
                    case EQ:
                        criteria.add(Restrictions.eq(key, restriction.getValue()));
                        break;
                    case NE:
                        criteria.add(Restrictions.ne(key, restriction.getValue()));
                        break;
                    case GT:
                        criteria.add(Restrictions.gt(key, restriction.getValue()));
                        break;
                    case GE:
                        criteria.add(Restrictions.ge(key, restriction.getValue()));
                        break;
                    case LT:
                        criteria.add(Restrictions.lt(key, restriction.getValue()));
                        break;
                    case LE:
                        criteria.add(Restrictions.le(key, restriction.getValue()));
                        break;
                    case IN:
                        criteria.add(Restrictions.in(key, (List)restriction.getValue()));
                        break;
                }
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
        criteria = null;
        limit = null;
        offset = null;
        order = null;
    }
    
    class Restriction {
        Object value;
        Comparator type;
        
        public Restriction(Object value, Comparator type) {
            this.value = value;
            this.type = type;
        }
        
        public Object getValue() {
            return value;
        }
        
        public Comparator getType() {
            return type;
        }
    }
    
    public enum Comparator {
        EQ, NE, GT, GE, LT, LE, IN;
    }
    
    class Between {
        Object min;
        Object max;
        
        public Between(Object min, Object max) {
            this.min = min;
            this.max = max;
        }
        
        public Object getMin() {
            return min;
        }
        
        public Object getMax() {
            return max;
        }
    }
}