package ru.airatyunusov.carservice.admin.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import ru.airatyunusov.carservice.BaseFragment
import ru.airatyunusov.carservice.R
import ru.airatyunusov.carservice.model.CategoryServices

class CategoryServicesFragment : BaseFragment() {

    private var nameEdiText: EditText? = null
    private var categoryServices: CategoryServices? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(TITLE)
        showButtonBack()
        setListenerArrowBack()

        nameEdiText = view.findViewById(R.id.nameCategoryEditText)

        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionExit -> {
                    signOut()
                    true
                }
                R.id.actionDelete -> {
                    removeCategory()
                    returnBack()
                    true
                }
                R.id.actionOk -> {
                    if (categoryServices == null) {
                        val id = reference.child(CHILD_NAME_CATEGORY).push().key ?: ""
                        saveCategory(id)
                    } else {
                        categoryServices?.id?.let { it1 -> saveCategory(it1) }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        arguments?.let {
            categoryServices = it.get(CATEGORY) as? CategoryServices
            nameEdiText?.setText(categoryServices?.name.toString())
        }

        if (categoryServices == null) {
            setMenu(R.menu.menu_save)
        } else {
            setMenu(R.menu.menu_save_delete)
        }
    }
    /**
     * Удалаяет из БД категорию
     * */

    private fun removeCategory() {
        val key = categoryServices?.id
        val childName = "categories"
        key?.let { reference.child(childName).child(it).removeValue() }
    }

    /**
     * Сохраняет в БД категорию услуг
     * */

    private fun saveCategory(id: String) {
        val name = nameEdiText?.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(
                requireContext(),
                MESSAGE_NOT_NULL_EDIT_TEXT,
                Toast.LENGTH_SHORT
            ).show()
        } else {

            val category = CategoryServices(id, name, getUserId())

            val childUpdates = hashMapOf<String, Any>(
                "/$CHILD_NAME_CATEGORY/$id" to category
            )
            reference.updateChildren(childUpdates)

            returnBack()
        }
    }

    companion object {
        private const val TITLE = "Категория услуг"
        private const val MESSAGE_NOT_NULL_EDIT_TEXT = "Поля для ввода не должны быть пустыми"
        private const val CHILD_NAME_CATEGORY = "categories"
        private const val CATEGORY = "category"

        fun newInstance(categoryServices: CategoryServices): CategoryServicesFragment {
            return CategoryServicesFragment().apply {
                arguments = bundleOf(CATEGORY to categoryServices)
            }
        }
    }
}
