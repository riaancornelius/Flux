package com.riaancornelius.flux.ui.issue;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.riaancornelius.flux.BaseActivity;
import com.riaancornelius.flux.R;
import com.riaancornelius.flux.api.SpiceCallback;
import com.riaancornelius.flux.jira.api.request.sprint.SprintReportRequest;
import com.riaancornelius.flux.jira.domain.sprint.report.Issue;
import com.riaancornelius.flux.jira.domain.sprint.report.SprintReport;
import com.riaancornelius.flux.ui.components.CustomPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: riaan.cornelius
 */
public class IssueListActivity extends BaseActivity implements SpiceCallback {

    private CustomPagerAdapter pagerAdapter;
    private ViewPager pager;
    private String lastRequestCacheKey;
    private int requestingId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_list);
        initUIComponents();

//        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        long boardId = getIntent().getExtras().getLong(BaseActivity.INTENT_KEY_BOARD_ID);
        long sprintId = getIntent().getExtras().getLong(BaseActivity.INTENT_KEY_SPRINT_ID);
        requestingId = getIntent().getExtras().getInt(BaseActivity.INTENT_KEY_REQUESTING_ID);
        performRequest(boardId, sprintId);
    }

//    // Create a tab listener that is called when the user changes tabs.
//    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
//        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//            // When the tab is selected, switch to the
//            // corresponding page in the ViewPager.
//            pager.setCurrentItem(tab.getPosition());
//        }
//
//        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//            // hide the given tab
//        }
//
//        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
//            // probably ignore this event
//        }
//    };

    private void initUIComponents() {
        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

        pager = (ViewPager) findViewById(R.id.issue_list_pager);
        pager.setAdapter(pagerAdapter);
//
//        pager.setOnPageChangeListener(
//                new ViewPager.SimpleOnPageChangeListener() {
//                    @Override
//                    public void onPageSelected(int position) {
//                        // When swiping between pages, select the
//                        // corresponding tab.
//                        getActionBar().setSelectedNavigationItem(position);
//                    }
//                });
    }

    private void performRequest(Long board, long sprintId) {
        beforeRequest();

        SprintReportRequest request = new SprintReportRequest(board, sprintId);
        lastRequestCacheKey = request.createCacheKey();
        spiceManager.execute(request, lastRequestCacheKey,
                DurationInMillis.ONE_WEEK, new ListSprintReportRequestListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(lastRequestCacheKey)) {
            outState.putString(KEY_LAST_REQUEST_CACHE_KEY, lastRequestCacheKey);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
            lastRequestCacheKey = savedInstanceState
                    .getString(KEY_LAST_REQUEST_CACHE_KEY);
            spiceManager.addListenerIfPending(SprintReport.class,
                    lastRequestCacheKey, new ListSprintReportRequestListener());
            spiceManager.getFromCache(SprintReport.class,
                    lastRequestCacheKey, DurationInMillis.ONE_WEEK,
                    new ListSprintReportRequestListener());
        }
    }

    @Override
    public void onRequestFinished() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private class ListSprintReportRequestListener implements
            RequestListener<SprintReport> {
        @Override
        public void onRequestFailure(SpiceException e) {
            IssueListActivity.this.afterRequest();
            Toast.makeText(IssueListActivity.this,
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            //IssueListActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(SprintReport sprintReport) {

            Log.d("IssueList", "onRequestSuccess. Got: " + sprintReport);

            // sprintReport could be null if contentManager.getFromCache(...)
            // doesn't return anything.
            if (sprintReport == null) {
                Log.d("IssueList", "sprintReport was null!!");
                return;
            }

            Log.d("IssueList", "sprintReport was not null");

            List<Issue> allIssues = new ArrayList<Issue>();
            List<Issue> puntedIssues = sprintReport.getContents().getPuntedIssues();
            List<Issue> incompletedIssues = sprintReport.getContents().getIncompletedIssues();
            List<Issue> completedIssues = sprintReport.getContents().getCompletedIssues();

            if ( incompletedIssues != null && !incompletedIssues.isEmpty()) {
                allIssues.addAll(incompletedIssues);
            }
            if ( completedIssues != null && !completedIssues.isEmpty()) {
                allIssues.addAll(completedIssues);
            }

            int allIssuesIndex = -1;
            if ( !allIssues.isEmpty()) {
                Log.d("IssueList", "adding view with all issues: " + allIssues.size());
                IssueListFragment allIssuesFragment = IssueListFragment.newInstance(allIssues, "All Issues");
                pagerAdapter.addFragment(IssueListFragment.KEY_ALL_ISSUES, allIssuesFragment);
                allIssuesIndex = pagerAdapter.getCount()-1;
//                getActionBar().addTab(getActionBar().newTab()
//                        .setText("All Issues")
//                        .setTabListener(tabListener));
            }

            //int puntedIssuesIndex = -1;
            if ( puntedIssues != null && !puntedIssues.isEmpty()) {
                Log.d("IssueList", "adding view with puntedIssues: " + puntedIssues.size());
                IssueListFragment puntedIssuesFragment = IssueListFragment.newInstance(puntedIssues, "Punted Issues");
                pagerAdapter.addFragment(IssueListFragment.KEY_PUNTED_ISSUES, puntedIssuesFragment);
                //puntedIssuesIndex = pagerAdapter.getCount()-1;
//                getActionBar().addTab(getActionBar().newTab()
//                        .setText("Punted Issues")
//                        .setTabListener(tabListener));
            }

            int incompleteIssuesIndex = -1;
            if ( incompletedIssues != null && !incompletedIssues.isEmpty()) {
                Log.d("IssueList", "adding view with incompletedIssues: " + incompletedIssues.size());
                IssueListFragment incompletedIssuesFragment = IssueListFragment.newInstance(incompletedIssues, "Open Issues");
                pagerAdapter.addFragment(IssueListFragment.KEY_PUNTED_ISSUES, incompletedIssuesFragment);
                incompleteIssuesIndex = pagerAdapter.getCount()-1;
//                getActionBar().addTab(getActionBar().newTab()
//                        .setText("Open Issues")
//                        .setTabListener(tabListener));
            }

            int completedIssuesIndex = -1;
            if ( completedIssues != null && !completedIssues.isEmpty()) {
                Log.d("IssueList", "adding view with completedIssues: " + completedIssues.size());
                IssueListFragment completedIssuesFragment = IssueListFragment.newInstance(completedIssues, "Completed Issues");
                pagerAdapter.addFragment(IssueListFragment.KEY_COMPLETE_ISSUES, completedIssuesFragment);
                completedIssuesIndex = pagerAdapter.getCount()-1;
//                getActionBar().addTab(getActionBar().newTab()
//                        .setText("Closed Issues")
//                        .setTabListener(tabListener));
            }

            setSelectedFragment(allIssuesIndex, incompleteIssuesIndex, completedIssuesIndex);

            Log.d("IssueList", "Done with onRequestSuccess!!");
            IssueListActivity.this.afterRequest();
        }
    }

    private void setSelectedFragment(int allIssuesIndex, int incompleteIssuesIndex, int completedIssuesIndex) {
        switch (requestingId){
            case R.id.uncompletedIssues:
                pager.setCurrentItem(incompleteIssuesIndex);
                break;
            case R.id.completedIssues:
                pager.setCurrentItem(completedIssuesIndex);
                break;
            case R.id.totalIssues:
                // This is the default option
            default:
                pager.setCurrentItem(allIssuesIndex);
        }
    }
}
