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
 * Copyright (C) hdsdi3g for hd3g.tv 4 oct. 2017
 * 
*/
package hd3gtv.mydmam.embddb.store;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

import hd3gtv.mydmam.embddb.store.FileBackend.StoreBackend;
import junit.framework.TestCase;

public class FileBackendTest extends TestCase {
	
	private final File backend_basedir;
	private final FileBackend all_backends;
	private static final String DB_NAME = "DB";
	
	public FileBackendTest() throws IOException {
		backend_basedir = new File(System.getProperty("user.home") + File.separator + "mydmam-debug");
		all_backends = new FileBackend(backend_basedir, UUID.fromString("00000000-0000-0000-0000-000000000000"));
	}
	
	public void testAll() throws IOException {
		StoreBackend backend = all_backends.get(DB_NAME, getClass().getSimpleName(), 1000);
		
		int size = 10000;
		
		/**
		 * Push datas in journal
		 */
		IntStream.range(0, size).parallel().forEach(i -> {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			byte[] payload = new byte[random.nextInt(1, 100)];
			random.nextBytes(payload);
			try {
				Item item = new Item(String.valueOf(i), payload);
				backend.writeInJournal(item, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		/**
		 * From https://www.mkyong.com/java/how-to-determine-a-prime-number-in-java/
		 */
		boolean[] primes = new boolean[10000];
		Arrays.fill(primes, true); // assume all integers are prime.
		primes[0] = primes[1] = false; // we know 0 and 1 are not prime.
		for (int i = 2; i < primes.length; i++) {
			// if the number is prime,
			// then go through all its multiples and make their values false.
			if (primes[i]) {
				for (int j = 2; i * j < primes.length; j++) {
					primes[i * j] = false;
				}
			}
		}
		
		final String update_path = "/updated";
		
		/**
		 * Update some datas in journal.
		 */
		IntStream.range(0, size).parallel().filter(i -> {
			return primes[i];
		}).forEach(i_prime -> {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			byte[] payload = new byte[random.nextInt(1, 100)];
			random.nextBytes(payload);
			try {
				Item item = new Item(update_path, String.valueOf(i_prime), payload);
				backend.writeInJournal(item, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		/**
		 * Flip the journal
		 */
		backend.doDurableWritesAndRotateJournal();
		
		/**
		 * No, read the datas
		 */
		int all_items = (int) backend.getAllDatas().parallel().map(entry -> {
			return Item.fromRawContent(entry);
		}).peek(item -> {
			assertFalse("Item " + item.getId() + " was deleted", item.isDeleted());
			int i = Integer.parseInt(item.getId());
			if (primes[i]) {
				assertEquals("Invalid path update for item " + item.getId(), update_path, item.getPath());
			}
		}).count();
		
		assertEquals("Invalid items retrived count", size, all_items);
		
		// TODO new update (remove non prime entries) + DurableWrites
		
		/*
		backend.cleanUpFiles();
		backend.contain(null);
		backend.getDatasByPath(path);
		backend.read(key);
		*/
		backend.purge();
		FileUtils.forceDelete(backend_basedir);
	}
	
	// TODO test fresh open with non-closed journals
	
}
