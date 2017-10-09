/*
 * This file is part of MyDMAM.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 11 sept. 2017
 * 
*/
package hd3gtv.mydmam.embddb.store;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.google.common.hash.Hashing;

import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.gson.GsonIgnore;
import hd3gtv.tools.Hexview;

@GsonIgnore
public final class Item implements ByteBufferExporter {
	
	private static Logger log = Logger.getLogger(Item.class);
	
	private String path;
	private String _id;
	private byte[] payload;
	private long created;
	private long updated;
	private long deleted;
	
	private transient ItemKey key;
	
	ItemKey getKey() {
		if (key == null) {
			key = new ItemKey(_id);
		}
		return key;
	}
	
	/**
	 * crc32 on payload
	 */
	byte[] getDigest() {
		return Hashing.crc32().hashBytes(payload).asBytes();
	}
	
	int estimateSize() {
		return _id.length() + path.length() + payload.length + 3 * 8;
	}
	
	void checkDigest(byte[] data) throws IOException {
		byte[] this_digest = getDigest();
		if (Arrays.equals(data, this_digest) == false) {
			if (log.isTraceEnabled()) {
				log.trace("Invalid raw datas: " + Hexview.LINESEPARATOR + this.getPayloadHexview());
			}
			throw new IOException("Invalid digest !");
		}
	}
	
	public static void writeNextBlock(DataOutputStream daos, byte[] value) throws IOException {
		daos.writeInt(value.length);
		if (value.length > 0) {
			daos.write(value);
		}
	}
	
	public static byte[] readNextBlock(DataInputStream dis) throws IOException {
		int size = dis.readInt();
		byte[] b = new byte[size];
		if (size > 0) {
			dis.read(b);
		}
		return b;
	}
	
	private Item() {
	}
	
	@Deprecated
	static Item fromRawContent(byte[] data) {
		ByteArrayInputStream bias = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bias);
		
		Item item = new Item();
		try {
			item._id = new String(readNextBlock(dis), MyDMAM.UTF8);
			item.path = new String(readNextBlock(dis), MyDMAM.UTF8);
			item.created = dis.readLong();
			item.updated = dis.readLong();
			item.deleted = dis.readLong();
			item.payload = readNextBlock(dis);
			item.checkDigest(readNextBlock(dis));
		} catch (IOException e) {
			log.error("Can't read raw StoreItem", e);
			return null;
		}
		
		return item;
	}
	
	public Item(String path, String _id, byte[] payload) {
		setId(_id).setPath(path).setPayload(payload);
		created = System.currentTimeMillis();
		deleted = Long.MAX_VALUE - (System.currentTimeMillis() * 10l);
	}
	
	public Item(String _id, byte[] payload) {
		this(null, _id, payload);
	}
	
	public Item setId(String _id) {
		this._id = requireNonEmpty(Objects.requireNonNull(_id, "\"_id\" can't to be null"), "\"_id\" can't to be empty");
		updated = System.currentTimeMillis();
		b_id = null;
		return this;
	}
	
	public Item setPath(String path) {
		if (path == null) {
			this.path = "";
		} else {
			this.path = path;
		}
		updated = System.currentTimeMillis();
		b_path = null;
		return this;
	}
	
	public Item setPayload(byte[] payload) {
		this.payload = Objects.requireNonNull(payload, "\"payload\" can't to be null");
		updated = System.currentTimeMillis();
		b_digest = null;
		return this;
	}
	
	public static String requireNonEmpty(String value, String message) {
		if (value.trim().isEmpty()) {
			throw new IndexOutOfBoundsException(message);
		}
		return value;
	}
	
	public static byte[] requireNonEmpty(byte[] value, String message) {
		if (value.length == 0) {
			throw new IndexOutOfBoundsException(message);
		}
		return value;
	}
	
	public String getId() {
		return _id;
	}
	
	public long getCreated() {
		return created;
	}
	
	public String getPath() {
		return path;
	}
	
	public byte[] getPayload() {
		return payload;
	}
	
	public long getUpdated() {
		return updated;
	}
	
	public boolean isDeleted() {
		return deleted < System.currentTimeMillis();
	}
	
	/**
	 * @return can be in past or future
	 */
	long getDeleteDate() {
		return deleted;
	}
	
	/**
	 * @return 0 if deleted
	 */
	long getActualTTL() {
		if (isDeleted()) {
			return 0;
		}
		return deleted - System.currentTimeMillis();
	}
	
	/**
	 * @param ttl if 0, no TTL, else real TTL (positive or negative)
	 */
	Item setTTL(long ttl) {
		if (ttl == 0) {
			deleted = Long.MAX_VALUE - (System.currentTimeMillis() * 10l);
		} else {
			deleted = System.currentTimeMillis() + ttl;
		}
		return this;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_id);
		sb.append(":");
		sb.append(path);
		if (isDeleted()) {
			sb.append(" DELETED");
		} else {
			sb.append(" (");
			sb.append(payload.length);
			sb.append(" bytes)");
		}
		return sb.toString();
	}
	
	public String getPayloadHexview() {
		return Hexview.tracelog(payload);
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Item other = (Item) obj;
		if (_id == null) {
			if (other._id != null) {
				return false;
			}
		} else if (!_id.equals(other._id)) {
			return false;
		}
		return true;
	}
	
	private transient byte[] b_id;
	private transient byte[] b_path;
	private transient byte[] b_digest;
	
	public void toByteBuffer(ByteBuffer write_buffer) throws IOException {
		if (b_id == null | b_path == null | b_digest == null) {
			getByteBufferWriteSize();
		}
		TransactionJournal.writeNextBlock(write_buffer, b_id);
		TransactionJournal.writeNextBlock(write_buffer, b_path);
		write_buffer.putLong(created);
		write_buffer.putLong(updated);
		write_buffer.putLong(deleted);
		TransactionJournal.writeNextBlock(write_buffer, payload);
		TransactionJournal.writeNextBlock(write_buffer, b_digest);
	}
	
	public int getByteBufferWriteSize() {
		if (b_id == null) {
			b_id = _id.getBytes(MyDMAM.UTF8);
		}
		if (b_path == null) {
			b_path = path.getBytes(MyDMAM.UTF8);
		}
		if (b_digest == null) {
			b_digest = getDigest();
		}
		return (4 + b_id.length) + (4 + b_path.length) + 8 + 8 + 8 + (4 + payload.length) + (4 + b_digest.length);
	}
	
	static Item fromByteBuffer(ByteBuffer read_buffer) {
		Item item = new Item();
		item._id = new String(TransactionJournal.readNextBlock(read_buffer), MyDMAM.UTF8);
		item.path = new String(TransactionJournal.readNextBlock(read_buffer), MyDMAM.UTF8);
		item.created = read_buffer.getLong();
		item.updated = read_buffer.getLong();
		item.deleted = read_buffer.getLong();
		item.payload = TransactionJournal.readNextBlock(read_buffer);
		
		try {
			item.checkDigest(TransactionJournal.readNextBlock(read_buffer));
		} catch (IOException e) {
			log.error("Can't read raw StoreItem", e);
			return null;
		}
		
		return item;
	}
	
}
