/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend;

import javax.persistence.EntityManager;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * @author Flemming Harms (flemming.harms@gmail.com)
 * @author Nicky Moelholm (moelholm@gmail.com)
 */
public final class DoWithEntityManager {

	private static final Log log = LoggerFactory.make();

	private DoWithEntityManager() {
	}

	public static <T> T execute(DoWithEntityManagerTask task) {
		TransactionManager transactionManager = null;
		Transaction activeJtaTransaction = null;

		EntityManager entityManager = null;
		try {
			transactionManager = TransactionManagerHolder.getTransactionManager();
			activeJtaTransaction = transactionManager.getTransaction();

			if ( activeJtaTransaction != null ) {
				log.tracef( "Suspending existing JTA transaction" );
				transactionManager.suspend();
			}
			transactionManager.begin();
			entityManager = EntityManagerFactoryHolder.getEntityManagerFactory().createEntityManager();
			entityManager.joinTransaction();

			T result = task.withEntityManager( entityManager );

			transactionManager.commit();
			return result;
		}
		catch (Exception e) {
			try {
				transactionManager.rollback();
			}
			catch (Exception e1) {
				log.trace( e1 );
			}
			throw log.errorWhenExecutingTask( e );

		}
		finally {
			entityManager.close();
			if ( activeJtaTransaction != null ) {
				try {
					log.tracef( "Resuming existing JTA transaction" );
					transactionManager.resume( activeJtaTransaction );
				}
				catch (Exception e) {
					throw log.errorWhenResumingTransaction( e );
				}
			}
		}
	}

	public interface DoWithEntityManagerTask {

		<T> T withEntityManager(EntityManager entityManager);
	}
}
