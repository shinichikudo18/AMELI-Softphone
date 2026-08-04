package cl.agnov.ameli.data

import cl.agnov.ameli.data.db.ContactDao
import cl.agnov.ameli.data.db.ContactEntity
import cl.agnov.ameli.sip.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Permite sustituir [RoomContactsRepository] por un fake en pruebas unitarias. */
interface ContactsRepository {
    val contacts: Flow<List<Contact>>
    suspend fun add(name: String, sipAddress: String)
    suspend fun remove(contact: Contact)
    suspend fun setFavorite(contact: Contact, isFavorite: Boolean)
}

/** Libreta de contactos local persistida con Room. */
class RoomContactsRepository(private val dao: ContactDao) : ContactsRepository {

    override val contacts: Flow<List<Contact>> =
        dao.observeAll().map { entities -> entities.map { it.toContact() } }

    override suspend fun add(name: String, sipAddress: String) {
        dao.insert(ContactEntity(name = name, sipAddress = sipAddress))
    }

    override suspend fun remove(contact: Contact) {
        dao.delete(
            ContactEntity(
                id = contact.id,
                name = contact.name,
                sipAddress = contact.sipAddress,
                isFavorite = contact.isFavorite,
            ),
        )
    }

    override suspend fun setFavorite(contact: Contact, isFavorite: Boolean) {
        dao.setFavorite(contact.id, isFavorite)
    }

    private fun ContactEntity.toContact() = Contact(id = id, name = name, sipAddress = sipAddress, isFavorite = isFavorite)
}
