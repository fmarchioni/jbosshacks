package org.demo.forge.jpa.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.demo.forge.jpa.model.Department;
import java.util.Iterator;
import org.demo.forge.jpa.model.Employee;

/**
 * Backing bean for Department entities.
 * <p/>
 * This class provides CRUD functionality for all Department entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class DepartmentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Department entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Department department;

	public Department getDepartment() {
		return this.department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Inject
	private Conversation conversation;

	@PersistenceContext(unitName = "demo-forge-jpa-persistence-unit", type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String create() {

		this.conversation.begin();
		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
			this.conversation.setTimeout(1800000L);
		}

		if (this.id == null) {
			this.department = this.example;
		} else {
			this.department = findById(getId());
		}
	}

	public Department findById(Integer id) {

		return this.entityManager.find(Department.class, id);
	}

	/*
	 * Support updating and deleting Department entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.department);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.department);
				return "view?faces-redirect=true&id="
						+ this.department.getDepartmentId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Department deletableEntity = findById(getId());
			Iterator<Employee> iterEmployees = deletableEntity.getEmployees()
					.iterator();
			for (; iterEmployees.hasNext();) {
				Employee nextInEmployees = iterEmployees.next();
				nextInEmployees.setDepartment(null);
				iterEmployees.remove();
				this.entityManager.merge(nextInEmployees);
			}
			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Department entities with pagination
	 */

	private int page;
	private long count;
	private List<Department> pageItems;

	private Department example = new Department();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Department getExample() {
		return this.example;
	}

	public void setExample(Department example) {
		this.example = example;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Department> root = countCriteria.from(Department.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Department> criteria = builder
				.createQuery(Department.class);
		root = criteria.from(Department.class);
		TypedQuery<Department> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Department> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String departmentName = this.example.getDepartmentName();
		if (departmentName != null && !"".equals(departmentName)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("departmentName")),
					'%' + departmentName.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Department> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Department entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Department> getAll() {

		CriteriaQuery<Department> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Department.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Department.class)))
				.getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final DepartmentBean ejbProxy = this.sessionContext
				.getBusinessObject(DepartmentBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(Integer.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Department) value).getDepartmentId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Department add = new Department();

	public Department getAdd() {
		return this.add;
	}

	public Department getAdded() {
		Department added = this.add;
		this.add = new Department();
		return added;
	}
}
