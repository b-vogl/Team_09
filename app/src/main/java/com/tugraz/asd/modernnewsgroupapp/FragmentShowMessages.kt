package com.tugraz.asd.modernnewsgroupapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.tugraz.asd.modernnewsgroupapp.databinding.FragmentShowMessageThreadsBinding
import com.tugraz.asd.modernnewsgroupapp.helper.ExpandableListAdapter
import com.tugraz.asd.modernnewsgroupapp.helper.Feedback
import kotlinx.android.synthetic.main.fragment_show_message_threads.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.nntp.Article

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentShowMessages : Fragment() {

    private lateinit var binding: FragmentShowMessageThreadsBinding
    private lateinit var viewModel: ServerObservable
    private lateinit var controller: NewsgroupController

    private var articles: Article? = null

    private val header : MutableList<Article> = ArrayList()
    private val body : MutableList<MutableList<Article>> = ArrayList()
    private var bodyBuffer : MutableList<Article> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShowMessageThreadsBinding.inflate(layoutInflater)
        viewModel = activity?.run {
            ViewModelProvider(this).get(ServerObservable::class.java)
        } ?: throw Exception("Invalid Activity")

        viewModel.controller.observe(viewLifecycleOwner) {
            if(!::controller.isInitialized) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.controller.value!!.fetchArticles()
                        controller = viewModel.controller.value!!
                        viewModel.controller.postValue(viewModel.controller.value)
                    }
                }
            } else {
                controller = viewModel.controller.value!!
                onControllerChange()
            }
        }

        return binding.root
    }

    private fun onControllerChange() {

        if(controller.currentArticle != null) {
            findNavController().navigate(R.id.action_FragmentMessageThreads_to_fragmentOpenThread)
        } else {

            if (controller.currentNewsgroup!!.alias.isNullOrEmpty()) {
                binding.headerText.text = controller.currentNewsgroup!!.name
            } else {
                binding.headerText.text = controller.currentNewsgroup!!.alias
            }

            articles = controller.currentArticles
            buildMessageList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener() {
            onButtonBackClick()
        }

        binding.buttonCreateThread.setOnClickListener() {
            onButtonCreateThreadClick()
        }
    }

    private fun buildMessageList() {

        if (articles == null) {
            Feedback.showInfo(requireView(), getString(R.string.feedback_no_message_threads))
        } else {
            showMessages(articles!!, 0)

            body.add(bodyBuffer)
            bodyBuffer = ArrayList()
            body.removeFirst()
            expandableView_show_messages.setAdapter(
                ExpandableListAdapter(
                    requireActivity(),
                    expandableView_show_messages,
                    header,
                    body,
                    viewModel
                )
            )
        }
    }

    private fun showMessages(article: Article, depth: Int) {

        if(article.articleNumberLong > 0 && !article.subjectIsReply()) {
            header.add(article)
            body.add(bodyBuffer)
            bodyBuffer = ArrayList()
        }

        if (article.kid != null) {
            if(article.kid.articleNumberLong > 0) {
                bodyBuffer.add(article.kid)
            }
            showMessages(article.kid, depth + 1)
        }
        if (article.next != null) {
            showMessages(article.next, depth)
        }
    }

    private fun onButtonBackClick() {
        controller.currentNewsgroup = null
        controller.currentArticles = null
        findNavController().navigate(R.id.action_FragmentMessageThreads_to_FragmentShowSubgroups)
    }

    private fun onButtonCreateThreadClick() {
        findNavController().navigate(R.id.action_FragmentMessageThreads_to_FragmentCreateThread)
    }
}