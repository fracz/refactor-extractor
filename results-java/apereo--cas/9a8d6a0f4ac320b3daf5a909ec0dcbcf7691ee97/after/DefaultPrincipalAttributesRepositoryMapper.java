package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Default mapper for converting {@link PrincipalAttributesRepository} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@RefreshScope
@Component(DefaultPrincipalAttributesRepositoryMapper.BEAN_NAME)
public class DefaultPrincipalAttributesRepositoryMapper implements PrincipalAttributesRepositoryMapper {
    /**
     * Name of this bean within the Spring context.
     */
    public static final String BEAN_NAME = "defaultPrincipalAttributesRepositoryMapper";

    @Override
    public void mapPrincipalRepository(final PrincipalAttributesRepository pr, final RegisteredServiceEditBean.ServiceData bean) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrPolicyBean = bean.getAttrRelease();
        if (pr instanceof DefaultPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.DEFAULT.toString());
        } else if (pr instanceof AbstractPrincipalAttributesRepository) {
            attrPolicyBean.setAttrOption(RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED.toString());

            final AbstractPrincipalAttributesRepository cc = (AbstractPrincipalAttributesRepository) pr;

            attrPolicyBean.setCachedExpiration(cc.getExpiration());
            attrPolicyBean.setCachedTimeUnit(cc.getTimeUnit());

            final IAttributeMerger merger = cc.getMergingStrategy().getAttributeMerger();

            if (merger != null) {
                if (merger instanceof NoncollidingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.ADD.toString());
                } else if (merger instanceof MultivaluedAttributeMerger) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.MULTIVALUED.toString());
                } else if (merger instanceof ReplacingAttributeAdder) {
                    attrPolicyBean.setMergingStrategy(RegisteredServiceAttributeReleasePolicyEditBean
                            .AttributeMergerTypes.REPLACE.toString());
                }
            }
        }
    }

    @Override
    public PrincipalAttributesRepository toPrincipalRepository(final RegisteredServiceEditBean.ServiceData data) {
        final RegisteredServiceAttributeReleasePolicyEditBean attrRelease = data.getAttrRelease();
        final String attrType = attrRelease.getAttrOption();

        if (StringUtils.equalsIgnoreCase(attrType, RegisteredServiceAttributeReleasePolicyEditBean.Types.CACHED
                .toString())) {
            return new CachingPrincipalAttributesRepository(attrRelease.getCachedTimeUnit().toUpperCase(),
                    attrRelease.getCachedExpiration());
        } else if (StringUtils.equalsIgnoreCase(attrType, RegisteredServiceAttributeReleasePolicyEditBean.Types
                .DEFAULT.toString())) {
            return new DefaultPrincipalAttributesRepository();
        }

        return null;
    }
}