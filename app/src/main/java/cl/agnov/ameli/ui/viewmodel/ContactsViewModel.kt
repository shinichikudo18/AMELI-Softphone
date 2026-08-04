package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.ContactsRepository
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Expone la libreta de contactos local y permite llamar/agregar/eliminar/marcar favorito/buscar. */
class ContactsViewModel(
    private val repository: ContactsRepository,
    private val callController: CallController,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val contacts: StateFlow<List<Contact>> = combine(repository.contacts, _searchQuery) { all, query ->
        if (query.isBlank()) {
            all
        } else {
            all.filter {
                it.name.contains(query, ignoreCase = true) || it.sipAddress.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun add(name: String, sipAddress: String) {
        viewModelScope.launch { repository.add(name, sipAddress) }
    }

    fun remove(contact: Contact) {
        viewModelScope.launch { repository.remove(contact) }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch { repository.setFavorite(contact, !contact.isFavorite) }
    }

    fun call(contact: Contact): Result<Unit> = callController.call(contact.sipAddress)
}
