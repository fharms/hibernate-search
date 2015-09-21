/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


/**
 * Entity for persisting {@link LuceneWork} in the database.
 * <p>
 * The {@link LuceneWork} is serialized as byte and stored in a byte array with an auto generated id and the index name.
 * </p>
 *
 * @author Flemming Harms (flemming.harms@gmail.com)
 * @author Nicky Moelholm (moelholm@gmail.com)
 */
@Entity
@Table(name = "lucene_work")
public class LuceneDatabaseWork implements Serializable {

	private static final long serialVersionUID = 0xCAFEBABE;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Lob
	@Column(length = 2147483647, nullable = false)
	private byte[] content;

	@Column(length = 255, nullable = false)
	private String indexName;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !( obj instanceof LuceneDatabaseWork ) ) {
			return false;
		}
		LuceneDatabaseWork other = (LuceneDatabaseWork) obj;
		if ( id != null ) {
			if ( !id.equals( other.id ) ) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
		return result;
	}

}
