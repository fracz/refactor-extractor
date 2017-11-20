package org.apereo.cas.mgmt.services.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.apereo.cas.mgmt.authentication.CasUserProfileFactory;
import org.apereo.cas.mgmt.services.web.beans.FormData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceDetails;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceItem;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RegexUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("manageRegisteredServicesMultiActionController")
public class ManageRegisteredServicesMultiActionController extends AbstractManagementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageRegisteredServicesMultiActionController.class);

    private static final String STATUS = "status";

    private final IPersonAttributeDao personAttributeDao;
    private final CasUserProfileFactory casUserProfileFactory;
    private final Service defaultService;

    private CasConfigurationProperties casProperties;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager              the services manager
     * @param personAttributeDao           the person attribute dao
     * @param webApplicationServiceFactory the web application service factory
     * @param defaultServiceUrl            the default service url
     */
    public ManageRegisteredServicesMultiActionController(
            final ServicesManager servicesManager,
            final IPersonAttributeDao personAttributeDao,
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final String defaultServiceUrl,
            final CasConfigurationProperties casProperties,
            final CasUserProfileFactory casUserProfileFactory) {
        super(servicesManager);
        this.personAttributeDao = personAttributeDao;
        this.defaultService = webApplicationServiceFactory.createService(defaultServiceUrl);
        this.casProperties = casProperties;
        this.casUserProfileFactory = casUserProfileFactory;
    }

    /**
     * Ensure default service exists.
     */
    private void ensureDefaultServiceExists() {
        this.servicesManager.load();
        final Collection<RegisteredService> c = this.servicesManager.getAllServices();
        if (c == null) {
            throw new IllegalStateException("Services cannot be empty");
        }

        if (!this.servicesManager.matchesExistingService(this.defaultService)) {
            final RegexRegisteredService svc = new RegexRegisteredService();
            svc.setServiceId('^' + this.defaultService.getId());
            svc.setName("Services Management Web Application");
            svc.setDescription(svc.getName());
            this.servicesManager.save(svc);
            this.servicesManager.load();
        }
    }

    /**
     * Authorization failure handling. Simply returns the view name.
     *
     * @return the view name.
     */
    @GetMapping(value = "/authorizationFailure")
    public String authorizationFailureView() {
        return "authorizationFailure";
    }

    /**
     * Logout handling. Simply returns the view name.
     *
     * @param request the request
     * @param session the session
     * @return the view name.
     */
    @GetMapping(value = "/logout.html")
    public String logoutView(final HttpServletRequest request, final HttpSession session) {
        LOGGER.debug("Invalidating application session...");
        session.invalidate();
        return "logout";
    }

    /**
     * Method to delete the RegisteredService by its ID. Will make sure
     * the default service that is the management app itself cannot be deleted
     * or the user will be locked out.
     *
     * @param idAsLong the id
     * @return the response entity
     */
    @GetMapping(value = "/deleteRegisteredService")
    public ResponseEntity<String> deleteRegisteredService(@RequestParam("id") final long idAsLong) {
        final RegisteredService svc = this.servicesManager.findServiceBy(this.defaultService);
        if (svc == null || svc.getId() == idAsLong) {
            return new ResponseEntity<>("The default service " + this.defaultService.getId() + " cannot be deleted. "
                    + "The definition is required for accessing the application.", HttpStatus.BAD_REQUEST);
        }

        final RegisteredService r = this.servicesManager.delete(idAsLong);
        if (r == null) {
            return new ResponseEntity<>("Service id " + idAsLong + " cannot be found.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(r.getName(), HttpStatus.OK);
    }

    @GetMapping(value = "/serviceDetails")
    public ResponseEntity<RegisteredServiceDetails> getServiceDetails(@RequestParam("id") final long idAsLong) {
        final RegisteredService svc = this.servicesManager.findServiceBy(idAsLong);
        final RegisteredServiceDetails details = new RegisteredServiceDetails();
        final RegisteredServiceAttributeReleasePolicy attrPolicy = svc.getAttributeReleasePolicy();
        details.setDescription(svc.getDescription());
        if (attrPolicy instanceof ScriptedRegisteredServiceAttributeReleasePolicy) {
            details.setAttributePolicy("SCRIPT");
        } else if (attrPolicy instanceof GroovyScriptAttributeReleasePolicy) {
            details.setAttributePolicy("GROOVY");
        } else if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
            details.setAttributePolicy("ALL");
        } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
            final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                details.setAttributePolicy("NONE");
            } else {
                details.setAttributePolicy("ALLOWED");
            }
        } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
            final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
            if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                details.setAttributePolicy("NONE");
            } else {
                details.setAttributePolicy("MAPPED");
            }
        } else if (attrPolicy instanceof DenyAllAttributeReleasePolicy) {
            details.setAttributePolicy("DENY");
        }
        details.setReleaseCredential(String.valueOf(attrPolicy.isAuthorizedToReleaseCredentialPassword()));
        details.setReleaseProxyTicket(String.valueOf(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket()));
        final RegisteredServiceProxyPolicy policy = svc.getProxyPolicy();
        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            details.setProxyPolicy("REFUSE");
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            details.setProxyPolicy("REGEX");
            details.setProxyPolicyValue(option.getPattern());
        }
        details.setLogoUrl(svc.getLogo());
        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    /**
     * Method to show the RegisteredServices.
     *
     * @param response the response
     * @return the Model and View to go to after the services are loaded.
     */
    @GetMapping(value = "/manage.html")
    public ModelAndView manage(final HttpServletResponse response) {
        ensureDefaultServiceExists();
        final Map<String, Object> model = new HashMap<>();
        model.put("defaultServiceUrl", this.defaultService.getId());
        model.put("type", this.casProperties.getServiceRegistry().getManagementType());
        model.put(STATUS, HttpServletResponse.SC_OK);
        return new ModelAndView("manage", model);
    }

    /**
     * Gets domains.
     *
     * @return the domains
     * @throws Exception the exception
     */
    @GetMapping(value = "/domains")
    public ResponseEntity<Collection<String>> getDomains() throws Exception {
        final Collection<String> data = this.servicesManager.getDomains();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }


    /**
     * Gets user.
     *
     * @param request  the request
     * @param response the response
     * @return the user
     * @throws Exception the exception
     */
    @GetMapping(value = "/user")
    public ResponseEntity<CasUserProfile> getUser(final HttpServletRequest request,
                                                  final HttpServletResponse response) throws Exception {
        final CasUserProfile data = casUserProfileFactory.from(request, response);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    /**
     * Gets services.
     *
     * @param domain the domain for which services will be retrieved
     * @return the services
     */
    @GetMapping(value = "/getServices")
    public ResponseEntity<List<RegisteredServiceItem>> getServices(@RequestParam final String domain) {
        ensureDefaultServiceExists();
        final List<RegisteredServiceItem> serviceItems = new ArrayList<>();
        final List<RegisteredService> services = new ArrayList<>(this.servicesManager.getServicesForDomain(domain));
        serviceItems.addAll(services.stream().map(this::createServiceItem).collect(Collectors.toList()));
        return new ResponseEntity<>(serviceItems, HttpStatus.OK);
    }

    /**
     * Method will filter all services in the register using the passed string a regular expression against the
     * service name, service id, and service description.
     *
     * @param query - a string representing text to search for
     * @return - the resulting services
     */
    @GetMapping(value = "/search")
    public ResponseEntity<List<RegisteredServiceItem>> search(@RequestParam final String query) {
        final Pattern pattern = RegexUtils.createPattern("^.*" + query + ".*$");
        final List<RegisteredServiceItem> serviceBeans = new ArrayList<>();
        final List<RegisteredService> services = this.servicesManager.getAllServices()
                .stream()
                .filter(service -> pattern.matcher(service.getServiceId()).lookingAt()
                        || pattern.matcher(service.getName()).lookingAt()
                        || pattern.matcher(service.getDescription()).lookingAt())
                .collect(Collectors.toList());
        serviceBeans.addAll(services.stream().map(this::createServiceItem).collect(Collectors.toList()));
        return new ResponseEntity<>(serviceBeans, HttpStatus.OK);
    }

    /**
     * Gets form data.
     *
     * @return the form data
     * @throws Exception the exception
     */
    @GetMapping(value = "formData")
    public ResponseEntity<FormData> getFormData() throws Exception {
        final FormData formData = new FormData();
        final Set<String> possibleUserAttributeNames = this.personAttributeDao.getPossibleUserAttributeNames();
        final List<String> possibleAttributeNames = new ArrayList<>();
        if (possibleUserAttributeNames != null) {
            possibleAttributeNames.addAll(possibleUserAttributeNames);
            Collections.sort(possibleAttributeNames);
        }
        formData.setAvailableAttributes(possibleAttributeNames);
        return new ResponseEntity<>(formData, HttpStatus.OK);
    }

    /**
     * Method will update the order of two services passed in.
     *
     * @param request  the request
     * @param response the response
     * @param svcs     the services to be updated
     */
    @PostMapping(value = "/updateOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(final HttpServletRequest request, final HttpServletResponse response,
                            @RequestBody final RegisteredServiceItem[] svcs) {
        final String id = svcs[0].getAssignedId();
        final RegisteredService svcA = this.servicesManager.findServiceBy(Long.parseLong(id));
        if (svcA == null) {
            throw new IllegalArgumentException("Service " + id + " cannot be found");
        }
        final String id2 = svcs[1].getAssignedId();
        final RegisteredService svcB = this.servicesManager.findServiceBy(Long.parseLong(id2));
        if (svcB == null) {
            throw new IllegalArgumentException("Service " + id2 + " cannot be found");
        }
        svcA.setEvaluationOrder(svcs[0].getEvalOrder());
        svcB.setEvaluationOrder(svcs[1].getEvalOrder());
        this.servicesManager.save(svcA);
        this.servicesManager.save(svcB);
    }

    private RegisteredServiceItem createServiceItem(final RegisteredService service) {
        final RegisteredServiceItem serviceItem = new RegisteredServiceItem();
        serviceItem.setAssignedId(String.valueOf(service.getId()));
        serviceItem.setEvalOrder(service.getEvaluationOrder());
        serviceItem.setName(service.getName());
        serviceItem.setServiceId(service.getServiceId());
        serviceItem.setDescription(service.getDescription().substring(0,service.getDescription().length() > 100 ? 99 : service.getDescription().length()));
        return serviceItem;
    }

}
