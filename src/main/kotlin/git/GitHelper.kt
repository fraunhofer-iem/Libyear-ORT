package git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import util.TimeHelper.isWithinOneYear
import java.io.File
import java.util.*

class GitHelper(repoUrl: String, outDir: File): Iterable<String> {

    private val git = Git.cloneRepository()
        .setCloneSubmodules(true)
        .setURI(repoUrl)
        .setDirectory(outDir)
        .call()

    private val commitFilter = DateFilter(Date().toInstant().toEpochMilli())

    private val commits = getOneCommitPerMonth(
        git.log()
            .setRevFilter(commitFilter)
            .call()
    ).toMutableList()

    fun close() {
        git.close()
    }

    private fun getOneCommitPerMonth(commits: Iterable<RevCommit>): List<RevCommit> {
        val calendar = Calendar.getInstance()
        val commitMap: MutableMap<Int, RevCommit> = mutableMapOf()
        commits.forEach { commit ->
            val commitTime = Date(commit.commitTime.toLong()*1000)
            calendar.time = commitTime
            val month = calendar[Calendar.MONTH]
            if (!commitMap.contains(month)) {
                commitMap[month] = commit
            }
        }

        return commitMap.values.toList()
    }

    private class DateFilter(val date: Long) : RevFilter() {
        override fun include(walker: RevWalk?, cmit: RevCommit?): Boolean {
            if (cmit != null) {
                return isWithinOneYear(date, cmit.commitTime.toLong()*1000) //example commitTime 1702406125
            }
            return false
        }

        override fun clone(): RevFilter {
            return this
        }
    }

    override fun iterator(): Iterator<String> {
        return object : Iterator<String> {
            override fun hasNext(): Boolean {
                return commits.isNotEmpty()
            }

            override fun next(): String {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                val commit = commits.removeFirst()

                git.checkout().setName(commit.name).call()
                println("Next commit $commit.name msg: ${commit.fullMessage}")
                return commit.name
            }
        }
    }
}
