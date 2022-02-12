package com.WeTechDigital.lifeTracker.view.fragment.ContactsAdd



import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.DeleteCache_Entity
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity
import com.WeTechDigital.lifeTracker.controller.RecyclerAdapterTrustedContacts
import com.WeTechDigital.lifeTracker.databinding.FragmentListContactsBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.ImageConverter
import com.WeTechDigital.lifeTracker.utils.LoadingDialog
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.material.snackbar.Snackbar
import com.WeTechDigital.lifeTracker.view.ManageTrustedContactsList
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.TrustedContactsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class ListContacts : Fragment(R.layout.fragment_list_contacts) {

    private var contactExceed = false
    private var contactLessThan = false
    private var emptyList = false
    private lateinit var trustedContactsViewModel: TrustedContactsViewModel
    private lateinit var binding: FragmentListContactsBinding
    private lateinit var mAdapter: RecyclerAdapterTrustedContacts
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel

    private var internetDisposable: Disposable? = null
    private var isInternetConnected = false
    private lateinit var data: TrustedContacts_Entity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentListContactsBinding.bind(view)



        initViewModel()
        offlineSetValue()





        binding.apply {
            saveContactList.setOnClickListener {

                when {
                    contactLessThan -> {
                        Toast.makeText(
                            requireContext(),
                            "You Have to add At-least 2 number",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    emptyList -> {
                        Toast.makeText(requireContext(), "Please Add Number", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        if (isInternetConnected) {
                            localDataBaseViewModel.readAllCache.observe(
                                viewLifecycleOwner,
                                { deleteList ->
                                    uploadDataToFireBase(deleteList)


                                })


                        } else {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    ManageTrustedContactsList::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                    }
                }

            }
            addNewContacts.setOnClickListener {
                when {
                    contactExceed -> {
                        Toast.makeText(
                            requireContext(),
                            "You Have Already Added 5 Contacts",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    else -> {

                        findNavController().navigate(R.id.action_listContacts_to_addContacts2)
                    }
                }

            }



            allAddedContacts.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = mAdapter
            }

            val itemTouchHelper = ItemTouchHelper(simpleCallback)
            itemTouchHelper.attachToRecyclerView(allAddedContacts)


        }


    }

    //-----------------------------------back ground thread--------------------//
    @DelicateCoroutinesApi
    private fun uploadDataToFireBase(
        deletelist: List<DeleteCache_Entity>
    ) {
        val mainJob = CoroutineScope(IO).launch {

            if (deletelist.isNotEmpty()) {
                for (path in deletelist) {
                    trustedContactsViewModel.deleteContact(path.deleteNumber)
                    localDataBaseViewModel.deleteCache(
                        DeleteCache_Entity(
                            path.deleteNumber
                        )
                    )
                }
            }

        }
        mainJob.invokeOnCompletion {
            Log.d(TAG, "uploadDataToFireBase: is complete")
            startActivity(
                Intent(
                    requireContext(),
                    ManageTrustedContactsList::class.java
                )
            )
            requireActivity().finish()

        }
        //-----------------------back ground thread------------------//

    }

    private fun offlineSetValue() {

        localDataBaseViewModel.readAllContacts.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: ${it.size}")
                contactExceed = it.size >= 5
                contactLessThan = it.size <= 1
                emptyList = it.isEmpty()
                mAdapter.setValue(it)

            }

        })
    }


    @DelicateCoroutinesApi
    private fun initViewModel() {
        trustedContactsViewModel = ViewModelProvider(this).get(TrustedContactsViewModel::class.java)
        mAdapter = RecyclerAdapterTrustedContacts(requireContext())
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)


    }

    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            when (direction) {
                ItemTouchHelper.LEFT -> {
                    //checking if the list contact less then one
                    if (contactLessThan) {
                        Toast.makeText(
                            requireContext(),
                            "To delete you must have al lest 2 contact ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        data = mAdapter.deleteItem(viewHolder.adapterPosition)
                        localDataBaseViewModel.addCache(
                            DeleteCache_Entity(
                                data.Phone
                            )
                        )

                        localDataBaseViewModel.deleteContacts(data)


                        mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                        LoadingDialog.loadingDialogStart(requireContext(), R.style.Custom)
                        localDataBaseViewModel.readAllContacts.observe(viewLifecycleOwner, {
                            Log.d(TAG, "onSwiped: current size after delete " + it.size)
                            contactExceed = it.size >= 5
                            contactLessThan = it.size <= 1
                            emptyList = it.isEmpty()

                            LoadingDialog.loadingDialogStop()
                            mAdapter.setValue(it)

                        })

                        Snackbar.make(
                            binding.allAddedContacts,
                            "This Contacts is deleted " + data.Phone,
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("Undo") {
                                localDataBaseViewModel.addContacts(data)
                                mAdapter.notifyItemInserted(viewHolder.adapterPosition)
                                localDataBaseViewModel.deleteCache(
                                    DeleteCache_Entity(
                                        data.Phone
                                    )
                                )
                                localDataBaseViewModel.readAllContacts.observe(
                                    viewLifecycleOwner,
                                    {
                                        Log.d(TAG, "onSwiped: current size after delete " + it.size)
                                        contactExceed = it.size >= 5
                                        contactLessThan = it.size <= 1
                                        emptyList = it.isEmpty()

                                        LoadingDialog.loadingDialogStop()
                                        mAdapter.setValue(it)

                                    })
                            }.show()
                    }


                }


            }


        }

    }


    ////----------------------netWork-------------------------//
    //checking internet and if the local data base is empty then it will collect data from firebase dataBase
    override fun onResume() {
        Log.d(TAG, "onResume: is called")
        super.onResume()
        //check internet connect in devices
        internetDisposable = ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToInternet ->
                isInternetConnected = isConnectedToInternet
                if (isConnectedToInternet) {
                    localDataBaseViewModel.readAllContacts.observe(viewLifecycleOwner, {
                        if (it.isEmpty()) {
                            trustedContactsViewModel.getContactsLiveData.observe(
                                viewLifecycleOwner,
                                { onlineList ->
                                    if (onlineList.isNotEmpty()) {
                                        binding.apply {
                                            addNewContacts.visibility = View.VISIBLE
                                            saveContactList.visibility = View.VISIBLE
                                        }

                                        lifecycleScope.launch {
                                            LoadingDialog.loadingDialogStart(
                                                requireContext(),
                                                R.style.Custom
                                            )
                                            for (model in onlineList) {
                                                val image = ImageConverter.getBitmap(
                                                    model.Image,
                                                    requireContext()
                                                )
                                                localDataBaseViewModel.addContacts(
                                                    TrustedContacts_Entity(

                                                        model.Phone,
                                                        model.Priority,
                                                        model.Name,
                                                        image
                                                    )
                                                )


                                            }
                                            LoadingDialog.loadingDialogStop()
                                        }
                                    } else {
                                        binding.apply {
                                            addNewContacts.visibility = View.VISIBLE
                                            saveContactList.visibility = View.VISIBLE
                                        }
                                    }


                                })


                        } else {
                            binding.apply {
                                addNewContacts.visibility = View.VISIBLE
                                saveContactList.visibility = View.VISIBLE
                            }
                        }
                    })
                } else {
                    binding.apply {
                        addNewContacts.visibility = View.VISIBLE
                        saveContactList.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "onResume: no internet")
                }
            }
    }



    override fun onPause() {
        super.onPause()
        safelyDispose(internetDisposable)
    }

    private fun safelyDispose(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
    }


}