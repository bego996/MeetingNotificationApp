/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.meetingnotification.ui.data

import kotlinx.coroutines.flow.Flow

class OfflineContactRepository(private val contactDao: ContactDao) : ContactRepository {

    override fun getAllContactsStream(): Flow<List<Contact>> = contactDao.getAllContacts()

    override fun getContactStream(id: Int): Flow<Contact?> = contactDao.getContact(id)

    override suspend fun insertItem(contact: Contact) = contactDao.insert(contact)

    override suspend fun deleteItem(contact: Contact) = contactDao.delete(contact)

    override suspend fun updateItem(contact: Contact) = contactDao.update(contact)
}
