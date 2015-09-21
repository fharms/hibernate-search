/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend;

import java.util.List;

/**
 * Wrap a {@link LuceneWork} to distinguish between when it's processed by
 * {@link AbstractDatabaseHibernateSearchController} and {@link DatabaseBackendQueueProcessor}
 * <P>
 * This is because the method applyWork is called twice - once by the Hibernate Search infrastructure itself and once
 * indirectly by the persistLuceneWorkListToDatabase.
 * </p>
 *
 * @author Flemming Harms (flemming.harms@gmail.com)
 * @author Nicky Moelholm (moelholm@gmail.com)
 */
public class DatabaseLuceneWorkWrapper extends LuceneWork {

	private static final long serialVersionUID = 0xCAFEBABE;

	private final List<LuceneWork> luceneWorkList;

	public DatabaseLuceneWorkWrapper(List<LuceneWork> luceneWorkList) {
		// TODO : FHARMS find the proper way to get the tenant ID
		super( "1", new Integer( 1 ), "1", DatabaseLuceneWorkWrapper.class );
		this.luceneWorkList = luceneWorkList;
	}

	public List<LuceneWork> getLuceneWorkList() {
		return this.luceneWorkList;
	}

	@Override
	public <P, R> R acceptIndexWorkVisitor(IndexWorkVisitor<P, R> visitor, P p) {
		return null;
	}
}
