package com.example.notes.ui

import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import com.example.notes.R
import com.example.notes.databinding.FragmentAddNoteBinding
import com.example.notes.databinding.FragmentHomeBinding
import com.example.notes.db.Note
import com.example.notes.db.NoteDatabase
import com.example.notes.util.toast
import kotlinx.coroutines.launch

class AddNoteFragment : BaseFragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private var note : Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        arguments?.let {
            note = AddNoteFragmentArgs.fromBundle(it).note
            binding.editTextTitle.setText(note?.title)
            binding.editTextNote.setText(note?.note)
        }

        binding.buttonSave.setOnClickListener { view ->
            val noteTitle = binding.editTextTitle.text.toString().trim()
            val noteBody = binding.editTextNote.text.toString().trim()

            if(noteTitle.isEmpty()) {
                binding.editTextTitle.error = "Title required"
                binding.editTextTitle.requestFocus()
                return@setOnClickListener
            }

            if(noteBody.isEmpty()) {
                binding.editTextNote.error = "Note required"
                binding.editTextNote.requestFocus()
                return@setOnClickListener
            }

            launch {
                context?.let {
                    val mNote = Note(noteTitle, noteBody)

                    if(note == null) {
                        NoteDatabase(it).getNoteDao().addNote(mNote)
                        it.toast("Note saved")
                    } else {
                        mNote.id = note!!.id
                        NoteDatabase(it).getNoteDao().updateNotes(mNote)
                        it.toast("Note updated")
                    }

                    val action = AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.delete -> {
                        // clearCompletedTasks()
                        if(note != null)
                            deleteNote()
                        else
                            context?.toast("Cannot delete")
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun deleteNote() {
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("Yes") { _,_ ->
                launch {
                    NoteDatabase(context).getNoteDao().deleteNote(note!!)
                    val action = AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment()
                    Navigation.findNavController(requireView()).navigate(action)
                }
            }
            setNegativeButton("No") { _,_ ->

            }
        }.create().show()
    }
}