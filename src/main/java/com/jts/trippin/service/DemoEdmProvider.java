/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.jts.trippin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jts.trippin.data.model.*;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

public class DemoEdmProvider extends CsdlAbstractEdmProvider {

    // Service Namespace
    public static final String NAMESPACE = "OData.TripPin";

    // Entity Set Names
    public static final String NAV_TO_CATEGORY = "Category";
    public static final String NAV_TO_PRODUCTS = "Products";

    //Action
    public static final FullQualifiedName ACTION_RESET_FQN = new FullQualifiedName(NAMESPACE, "Reset");

    // Function
    public static final FullQualifiedName FUNCTION_COUNT_CATEGORIES_FQN
            = new FullQualifiedName(NAMESPACE, "CountCategories");

    // Function/Action Parameters
    public static final String PARAMETER_AMOUNT = "Amount";

    @Override
    public List<CsdlAction> getActions(final FullQualifiedName actionName) {
        if (actionName.equals(ACTION_RESET_FQN)) {
            // It is allowed to overload actions, so we have to provide a list of Actions for each action name
            final List<CsdlAction> actions = new ArrayList<>();

            // Create parameters
            final List<CsdlParameter> parameters = new ArrayList<>();
            final CsdlParameter parameter = new CsdlParameter();
            parameter.setName(PARAMETER_AMOUNT);
            parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            parameters.add(parameter);

            // Create the Csdl Action
            final CsdlAction action = new CsdlAction();
            action.setName(ACTION_RESET_FQN.getName());
            action.setParameters(parameters);
            actions.add(action);

            return actions;
        }

        return null;
    }

    @Override
    public CsdlActionImport getActionImport(final FullQualifiedName entityContainer, final String actionImportName) {
        if (entityContainer.equals(EntityContainer.CONTAINER)) {
            if (actionImportName.equals(ACTION_RESET_FQN.getName())) {
                return new CsdlActionImport()
                        .setName(actionImportName)
                        .setAction(ACTION_RESET_FQN);
            }
        }

        return null;
    }

    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        if (functionName.equals(FUNCTION_COUNT_CATEGORIES_FQN)) {
            // It is allowed to overload functions, so we have to provide a list of functions for each function name
            final List<CsdlFunction> functions = new ArrayList<>();

            // Create the parameter for the function
            final CsdlParameter parameterAmount = new CsdlParameter();
            parameterAmount.setName(PARAMETER_AMOUNT);
            parameterAmount.setNullable(false);
            parameterAmount.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

            // Create the return type of the function
            final CsdlReturnType returnType = new CsdlReturnType();
            returnType.setCollection(true);
            returnType.setType(Category.ET_FQN);

            // Create the function
            final CsdlFunction function = new CsdlFunction();
            function.setName(FUNCTION_COUNT_CATEGORIES_FQN.getName())
                    .setParameters(Arrays.asList(parameterAmount))
                    .setReturnType(returnType);
            functions.add(function);

            return functions;
        }

        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) {
        if (entityContainer.equals(EntityContainer.CONTAINER)) {
            if (functionImportName.equals(FUNCTION_COUNT_CATEGORIES_FQN.getName())) {
                return new CsdlFunctionImport()
                        .setName(functionImportName)
                        .setFunction(FUNCTION_COUNT_CATEGORIES_FQN)
                        .setEntitySet(Category.ES_NAME)
                        .setIncludeInServiceDocument(true);
            }
        }

        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

        // this method is called for each EntityType that are configured in the Schema
        CsdlEntityType entityType = null;

        if (entityTypeName.equals(Product.FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: many-to-one, null not allowed (product must have a category)
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName(NAV_TO_CATEGORY)
                    .setType(Category.ET_FQN).setNullable(true).setPartner("Products");
            List<CsdlNavigationProperty> navPropList = new ArrayList<>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(Product.FQN.getName());
            entityType.setProperties(Arrays.asList(id, name, description));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);

        } else if (entityTypeName.equals(Category.ET_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName(NAV_TO_PRODUCTS)
                    .setType(Product.FQN).setCollection(true).setPartner("Category");
            List<CsdlNavigationProperty> navPropList = new ArrayList<>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(Category.ET_FQN.getName());
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        } else if (entityTypeName.equals(Advertisement.ET_FQN)) {
            CsdlProperty id = new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Guid.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name").setType(EdmPrimitiveTypeKind.String
                    .getFullQualifiedName());
            CsdlProperty airDate = new CsdlProperty().setName("AirDate").setType(EdmPrimitiveTypeKind.DateTimeOffset
                    .getFullQualifiedName());

            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            entityType = new CsdlEntityType();
            entityType.setName(Advertisement.ET_FQN.getName());
            entityType.setProperties(Arrays.asList(id, name, airDate));
            entityType.setKey(Collections.singletonList(propertyRef));
            entityType.setHasStream(true);
        }

        return entityType;

    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        CsdlEntitySet entitySet = null;

        if (entityContainer.equals(EntityContainer.CONTAINER)) {

            if (entitySetName.equals(ProductSet.NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ProductSet.NAME);
                entitySet.setType(Product.FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Categories"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Category"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);

            } else if (entitySetName.equals(Category.ES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(Category.ES_NAME);
                entitySet.setType(Category.ET_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Products"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Products"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(Advertisement.ES_NAME)) {
                entitySet = new CsdlEntitySet();
                entitySet.setName(Advertisement.ES_NAME);
                entitySet.setType(Advertisement.ET_FQN);
            }
        }

        return entitySet;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the service document at
        // e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(EntityContainer.CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(EntityContainer.CONTAINER);
            return entityContainerInfo;
        }

        return null;
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(Product.FQN));
        entityTypes.add(getEntityType(Category.ET_FQN));
        entityTypes.add(getEntityType(Advertisement.ET_FQN));
        schema.setEntityTypes(entityTypes);

        // add actions
        List<CsdlAction> actions = new ArrayList<>(getActions(ACTION_RESET_FQN));
        schema.setActions(actions);

        // add functions
        List<CsdlFunction> functions = new ArrayList<>(getFunctions(FUNCTION_COUNT_CATEGORIES_FQN));
        schema.setFunctions(functions);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {

        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        entitySets.add(getEntitySet(EntityContainer.CONTAINER, ProductSet.NAME));
        entitySets.add(getEntitySet(EntityContainer.CONTAINER, Category.ES_NAME));
        entitySets.add(getEntitySet(EntityContainer.CONTAINER, Advertisement.ES_NAME));

        // Create function imports
        List<CsdlFunctionImport> functionImports = new ArrayList<>();
        functionImports.add(getFunctionImport(EntityContainer.CONTAINER, FUNCTION_COUNT_CATEGORIES_FQN.getName()));

        // Create action imports
        List<CsdlActionImport> actionImports = new ArrayList<>();
        actionImports.add(getActionImport(EntityContainer.CONTAINER, ACTION_RESET_FQN.getName()));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(EntityContainer.CONTAINER.getName());
        entityContainer.setActionImports(actionImports);
        entityContainer.setFunctionImports(functionImports);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

}
