package com.irateam.vkplayer.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapters.AudioRecyclerViewAdapter
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.ui.CustomItemAnimator
import org.greenrobot.eventbus.EventBus

class AudioListFragment : Fragment() {

    private val eventBus = EventBus.getDefault()
    private val adapter = AudioRecyclerViewAdapter()

    private lateinit var query: Query<List<Audio>>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_audio_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = CustomItemAnimator()

        refreshLayout = view.findViewById(R.id.refresh_layout) as SwipeRefreshLayout
        refreshLayout.setColorSchemeResources(
                R.color.accent,
                R.color.primary)
        refreshLayout.setOnRefreshListener { executeQuery() }

        eventBus.register(adapter)
        executeQuery()
        /*TODO:
        refreshLayout.setOnRefreshListener(() -> {
            if (actionMode != null)
                actionMode.finish();

            if (audioAdapter.isSortMode())
                audioAdapter.setSortMode(false);
        }); */
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_sort -> {
            adapter.setSortMode(true)
            item.isVisible = false
            menu.findItem(R.id.action_sort_done).isVisible = true
            true
        }
        R.id.action_sort_done -> {
            adapter.setSortMode(false)
            item.isVisible = false
            menu.findItem(R.id.action_sort).isVisible = true
            true
        }
        else -> false
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(adapter)
    }

    private fun executeQuery() {
        refreshLayout.isRefreshing = true
        query.execute(SimpleCallback
                .success { audios: List<Audio> -> adapter.setAudios(audios) }
                .finish { refreshLayout.isRefreshing = false })
    }

    companion object {

        @JvmStatic
        fun newInstance(query: Query<List<Audio>>): AudioListFragment {
            val fragment = AudioListFragment()
            fragment.query = query
            return fragment
        }
    }

    /*listView = (DragSortListView) findViewById(R.id.list);
        listView.setAdapter(audioAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            List<Audio> list = audioAdapter.getListByPosition(position);
            playerService.setPlaylist(list);
            playerService.play(audioAdapter.getPosition(position));
            if (actionMode != null)
                actionMode.finish();

            MenuItem item = navigationView.getMenu().getItem(0);
            boolean isFromSearch = audioAdapter.belongsToSearchList(position);
            if (!isFromSearch) {
                item.setChecked(true);
                getSupportActionBar().setTitle(item.getTitle());
            }
            if (!isFromSearch || item.isChecked()) {
                audioAdapter.setOriginalList(list);
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            performCheck(position);
            return true;
        });
        listView.setDropListener(audioAdapter::drop);*/

}
