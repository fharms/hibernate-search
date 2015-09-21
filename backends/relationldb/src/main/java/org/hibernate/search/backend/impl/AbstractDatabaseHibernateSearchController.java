/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.search.backend.DatabaseLuceneWorkWrapper;
import org.hibernate.search.backend.DoWithEntityManager;
import org.hibernate.search.backend.DoWithEntityManager.DoWithEntityManagerTask;
import org.hibernate.search.backend.LuceneWork;
import org.hibernate.search.backend.model.LuceneDatabaseWork;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * @author Flemming Harms (flemming.harms@gmail.com)
 * @author Nicky Moelholm (moelholm@gmail.com)
 */
public abstract class AbstractDatabaseHibernateSearchController {

	private static final Log log = LoggerFactory.make();

	protected abstract SearchIntegrator getSearchIntegrator();

	private int luceneWorkBatchSize = 100;

	/**
	 * Set the number of {@link LuceneDatabaseWork} is should process in one transaction <br>
	 * <p>
	 * Default is 100
	 * </p>
	 */
	protected void setLuceneWorkBatchSize(int size) {
		this.luceneWorkBatchSize = size;
	}

	/**
	 * Process the Lucene worker queue by retrieve all the works from the table in chunk of
	 * <code>luceneWorkBatchSize</code> and remove them when they are processed.
	 */
	public void processWorkQueue() {

		final SearchIntegrator integrator = getSearchIntegrator();

		try {

			DoWithEntityManager.execute( new DoWithEntityManagerTask() {

				@Override
				@SuppressWarnings("unchecked")
				public Void withEntityManager(EntityManager entityManager) {
					log.debug( "Work queue processing started" );

					String queryAsString = String.format( Locale.ROOT, "from %s order by id asc", LuceneDatabaseWork.class.getName() );
					TypedQuery<LuceneDatabaseWork> query = entityManager.createQuery( queryAsString, LuceneDatabaseWork.class );
					query.setMaxResults( luceneWorkBatchSize );
					List<LuceneDatabaseWork> resultList = query.getResultList();
					log.debug( String.format( Locale.ROOT, "Found [%s] %ss", resultList.size(), LuceneDatabaseWork.class.getSimpleName() ) );

					for ( LuceneDatabaseWork luceneWork : resultList ) {
						String indexName = luceneWork.getIndexName();
						IndexManager indexManager = integrator.getIndexManager( indexName );
						if ( indexManager == null ) {
							log.messageReceivedForUndefinedIndex( indexName );
							continue;
						}
						log.debug( String.format( Locale.ROOT, "Indexing [%s] [id=%s]", indexName, luceneWork.getId() ) );
						List<LuceneWork> queue = indexManager.getSerializer().toLuceneWorks( luceneWork.getContent() );
						List<LuceneWork> worker = new ArrayList<>((Collection<? extends LuceneWork>) new DatabaseLuceneWorkWrapper( queue ));
						indexManager.performOperations( worker, null );
						entityManager.remove( luceneWork );
					}

					log.debug( "Work queue processing finished" );
					return null;
				}

			} );

		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
		finally {
		}
	}

}
