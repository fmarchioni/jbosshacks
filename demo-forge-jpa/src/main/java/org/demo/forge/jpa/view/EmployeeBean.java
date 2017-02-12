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

import org.demo.forge.jpa.model.Employee;
import org.demo.forge.jpa.model.Department;

/**
 * Backing bean for Employee entities.
 * <p/>
 * This class provides CRUD functionality for all Employee entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class EmployeeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Employee entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Employee employee;

	public Employee getEmployee() {
		return this.employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
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
			this.employee = this.example;
		} else {
			this.employee = findById(getId());
		}
	}

	public Employee findById(Integer id) {

		return this.entityManager.find(Employee.class, id);
	}

	/*
	 * Support updating and deleting Employee entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.employee);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.employee);
				return "view?faces-redirect=true&id="
						+ this.employee.getEmployeeId();
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
			Employee deletableEntity = findById(getId());
			Department department = deletableEntity.getDepartment();
			department.getEmployees().remove(deletableEntity);
			deletableEntity.setDepartment(null);
			this.entityManager.merge(department);
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
	 * Support searching Employee entities with pagination
	 */

	private int page;
	private long count;
	private List<Employee> pageItems;

	private Employee example = new Employee();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Employee getExample() {
		return this.example;
	}

	public void setExample(Employee example) {
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
		Root<Employee> root = countCriteria.from(Employee.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Employee> criteria = builder.createQuery(Employee.class);
		root = criteria.from(Employee.class);
		TypedQuery<Employee> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Employee> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Department department = this.example.getDepartment();
		if (department != null) {
			predicatesList
					.add(builder.equal(root.get("department"), department));
		}
		String employeeName = this.example.getEmployeeName();
		if (employeeName != null && !"".equals(employeeName)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("employeeName")),
					'%' + employeeName.toLowerCase() + '%'));
		}
		int employeeSalary = this.example.getEmployeeSalary();
		if (employeeSalary != 0) {
			predicatesList.add(builder.equal(root.get("employeeSalary"),
					employeeSalary));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Employee> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Employee entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Employee> getAll() {

		CriteriaQuery<Employee> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Employee.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Employee.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final EmployeeBean ejbProxy = this.sessionContext
				.getBusinessObject(EmployeeBean.class);

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

				return String.valueOf(((Employee) value).getEmployeeId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Employee add = new Employee();

	public Employee getAdd() {
		return this.add;
	}

	public Employee getAdded() {
		Employee added = this.add;
		this.add = new Employee();
		return added;
	}
}
