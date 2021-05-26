package com.tugraz.asd.modernnewsgroupapp

import com.tugraz.asd.modernnewsgroupapp.vo.Newsgroup
import com.tugraz.asd.modernnewsgroupapp.vo.NewsgroupServer
import org.apache.commons.net.nntp.*
import java.net.UnknownHostException
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class NewsgroupConnection (private var server: NewsgroupServer){
    private lateinit var resp: Iterable<Article>
    private lateinit var article: Article
    private  var client: NNTPClient = NNTPClient()

    private fun ensureConnection() {
        if(!client.isConnected) {
            try {
                client.connect(server.host, server.port)
            } catch (e: Exception) {
                when(e) {
                    is UnknownHostException -> {
                        throw NewsgroupConnectionException("Unknown host while connecting to newsgroup server")
                    }
                    else -> {
                        throw NewsgroupConnectionException("IOException while connecting to newsgroup server " + server.host + ": " + e.message)
                    }
                }
            }
        }
    }

    fun getNewsGroups(): ArrayList<Newsgroup> {
        ensureConnection()
        val response = client.listNewsgroups()
        val groups: ArrayList<Newsgroup> = ArrayList()

        for (group in response) {
            groups.add(Newsgroup(name = group.newsgroup, newsgroupServerId = server.id, firstArticle = group.firstArticleLong, lastArticle = group.lastArticleLong))
        }
        return groups
    }

    fun getArticleHeaders(sg: Newsgroup?): Article{
        ensureConnection()
        if (sg != null) {
            print("name of ng to select: " + sg.name)
            if(client.selectNewsgroup(sg.name))
                print("Newsgroup selected")
            else
                print("Failed select newsgroup")
        }
        //var response = client.listNewsgroups()
        if (sg != null) {
            resp = client.iterateArticleInfo(sg.firstArticle, sg.lastArticle)
            var threader = Threader()
            var graph = threader.thread(resp)
            article = (graph as Article?)!!
        }
        return article
    }

    fun getArticleBody(sg: Newsgroup?, id: Long)
    {

    }

    fun postArticle(newsgroup: Newsgroup, from: String, subject: String, message: String): Boolean {
        ensureConnection()

        if(!client.isAllowedToPost) return false

        client.selectNewsgroup(newsgroup.name)

        val writer = client.postArticle() ?: return false

        val header = SimpleNNTPHeader(from, subject)
        header.addNewsgroup(newsgroup.name)
        writer.write(header.toString());
        writer.write(message);
        writer.close();
        client.completePendingCommand()
        return true


    }

    /*
        Custom Exception class for newsgroup connection
     */
    class NewsgroupConnectionException(message:String): Exception(message)

}
