/******************************************************************
 * File:        StoreAPI.java
 * Created by:  Dave Reynolds
 * Created on:  22 Jan 2013
 *
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.registry.store;

import java.util.List;

import com.epimorphics.registry.core.Description;
import com.epimorphics.registry.core.Register;
import com.epimorphics.registry.core.RegisterItem;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Abstract interface onto the underlying versioned RDF storage and
 * indexing system. Should support non-RDF storage options as well as
 * straight triple stores.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface StoreAPI {

    // --- Methods for access versions of resource descriptions ---

    /**
     * Return the register/item/entity at the given address or null if there is none such.
     * No version processing.
     * @param forupdate if true then the operation will check if the resource is locked if so will block until the lock is released, then a new lock will be taken
     */
    public Description getDescription(String uri, boolean forupdate);

    /**
     * Return the current version of the resource.
     * @param uri the uri of the base VersionedThing
     * @param forupdate if true then the operation will check if the resource is locked if so will block until the lock is released, then a new lock will be taken
     * @return Description containing a merge of the selected Version and the root VersionedThing
     */
    public Description getCurrentVersion(String uri, boolean forupdate);

    /**
     * Release the "forupdate" lock on the given URI (should be a Register or RegisterItem).
     * Throws an error if there is no such lock.
     */
    public void unlock(String uri);

    /**
     * Return a specific version of a versioned resource .
     * @param uri the uri of the Version instance to be retrieved
     * @return Description containing a merge of the selected Version and the root VersionedThing
     */
    public Description getVersion(String uri);

    /**
     * Return a specific version of a versioned resource .
     * @param uri the uri of the base VersionedThing
     * @param time the timestamp at which the desired version was valid
     * @return Description containing a merge of the selected Version and the root VersionedThing
     */
    public Description getVersionAt(String uri, long time);

    /**
     * List all known verisons of a VersionedThing
     * @param uri the uri of the base VersionedThing
     */
    public List<VersionInfo> listVersions(String uri);

    // --- Methods for accessing linked register resources ---

    /**
     * Return a RegisterItem, optionally along with the associated entity definition.
     * The RegisterItem root resource and current version will be merged. If the entity
     * is fetched and if the entity is itself a VersionedThing then its version information
     * will also be fetched and merged.
     * @param uri uri of the base (VersionedThing) RegisterItem to be fetched
     * @param withEntity if true then the entity defined
     * @param forupdate if true then the operation will check if the resource is locked if so will block until the lock is released, then a new lock will be taken
     */
    public RegisterItem getItem(String uri, boolean withEntity, boolean forupdate);

    /**
     * Fetch the entity specified by the Register item and add it to the item's data structure.
     */
    public Resource getEntity(RegisterItem item);

    /**
     * Fetch all RegisterItems. Fetches all items, including NotAccepted items.
     * All retrieved resources will be version-flattened if required.
     * @param register the register to be updated with a list of its members
     * @param withEntity if true then for each member fetched, the associated entity will also be fetched
     */
    public List<RegisterItem> fetchMembers(Register register, boolean withEntity);

    /**
     * List all members of a register. This gives a low cost way to enumerate the core information
     * on the members without fetching and merging version and entity descriptions.
     */
    public List<RegisterEntryInfo> listMembers(Register register);

    // TODO need version  of this that retrieves versions of items as valid at the time a specific register version was created

    // --- Methods for updating information in the store ---


    /**
     * Add a new registered item to a parent register.
     * Initializes the versioning of the new RegisterItem.
     * If the entity of the item is a Register then the versioning of the new sub-register will be initialized.
     * Otherwise a new entity graph will be created for the entity.
     * Does NOT release the "forupdate" lock on the register to allow multiple entries to be added.
     */
    public void addToRegister(Register register, RegisterItem item);

    /**
     * Update the metadata for a register, managing the versioning information.
     * Releases the "forupdate" lock on register.
     */
    public void update(Register register);

    /**
     * Update a Register item
     * @param withEntity if true then a new version of the entity will be saved, if false then
     * just the item metadata will be udpated.
     * Releases the "forupdate" lock on item.
     */
    public void update(RegisterItem item, boolean withEntity);


    // --- misc operations ---

    /**
     * Tests if the register contains an item with the given notation (relative URI)
     */
    public boolean contains(Register register, String notation);

    /**
     * Loads a bootstrap file which defines the initial state of the registry. Should at least
     * contain a root register but may also define vocabulary information or system registers.
     * Non-sparql store implementations may require non-trivial processing to separate out the
     * different system registers and may impose constraints on the file packaging supported.
     */
    public void loadBootstrap(String filename);

}
