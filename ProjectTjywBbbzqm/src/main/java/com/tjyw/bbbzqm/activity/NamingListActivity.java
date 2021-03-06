package com.tjyw.bbbzqm.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.tjyw.atom.network.IllegalRequestException;
import com.tjyw.atom.network.conf.IApiField;
import com.tjyw.atom.network.param.ListRequestParam;
import com.tjyw.atom.network.presenter.IPost;
import com.tjyw.atom.network.presenter.NamingPresenter;
import com.tjyw.atom.network.presenter.listener.OnApiFavoritePostListener;
import com.tjyw.atom.network.presenter.listener.OnApiPostErrorListener;
import com.tjyw.atom.network.presenter.listener.OnApiPostNamingListener;
import com.tjyw.atom.network.result.RIdentifyResult;
import com.tjyw.atom.network.result.RNameDefinition;
import atom.pub.inject.From;
import com.tjyw.bbbzqm.ClientQmjmApplication;
import com.tjyw.bbbzqm.R;
import com.tjyw.bbbzqm.adapter.NameMasterAdapter;
import com.tjyw.bbbzqm.factory.IClientActivityLaunchFactory;
import com.tjyw.bbbzqm.item.NamingWordItem;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import nucleus.factory.RequiresPresenter;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by stephen on 14/08/2017.
 */
@RequiresPresenter(NamingPresenter.class)
public class NamingListActivity extends BaseToolbarActivity<NamingPresenter<NamingListActivity>> implements
        OnApiPostErrorListener,
        OnApiPostNamingListener,
        OnApiFavoritePostListener.PostAddListener, OnApiFavoritePostListener.PostRemoveListener {

    @From(R.id.namingListContainer)
    protected RecyclerView namingListContainer;

    @From(R.id.payOrderRepeatPay)
    protected TextView payOrderRepeatPay;

    protected FastItemAdapter<NamingWordItem> nameDefinitionAdapter;

    protected ListRequestParam listRequestParam;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listRequestParam = (ListRequestParam) pGetSerializableExtra(IApiField.P.param);
        if (null == listRequestParam) {
            finish();
            return ;
        } else {
            setContentView(R.layout.atom_naming_list);
            tSetToolBar(getString(R.string.atom_pub_resStringNamingList));

            immersionBarWith()
                    .fitsSystemWindows(true)
                    .statusBarColor(R.color.colorPrimary)
                    .statusBarDarkFont(STATUSBAR_DARK_FONT)
                    .init();
        }

        nameDefinitionAdapter = new FastItemAdapter<NamingWordItem>();
        nameDefinitionAdapter.withItemEvent(new ClickEventHook<NamingWordItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof NamingWordItem.NamingWordHolder) {
                    return ((NamingWordItem.NamingWordHolder) viewHolder).getNameWordCollect();
                } else {
                    return super.onBind(viewHolder);
                }
            }

            @Override
            public void onClick(View v, int position, FastAdapter<NamingWordItem> fastAdapter, NamingWordItem item) {
                if (item.src.favorite && item.src.id > 0) {
                    getPresenter().postFavoriteRemove(item.src.id, item);
                } else {
                    getPresenter().postFavoriteAdd(
                            item.src.surname,
                            item.src.getGivenName(),
                            item.src.day,
                            item.src.gender,
                            item
                    );
                }
            }
        }).withOnClickListener(new FastAdapter.OnClickListener<NamingWordItem>() {
            @Override
            public boolean onClick(View v, IAdapter<NamingWordItem> adapter, NamingWordItem item, int position) {
                ListRequestParam param = new ListRequestParam(item.src);
                IClientActivityLaunchFactory.launchExplainMasterActivity(NamingListActivity.this, param, 100);
                return true;
            }
        });

        namingListContainer.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        namingListContainer.setAdapter(nameDefinitionAdapter);
        namingListContainer.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(ClientQmjmApplication.getContext())
                        .color(R.color.atom_pub_resColorDivider)
                        .sizeResId(R.dimen.atom_pubResDimenRecyclerViewDividerSize)
                        .marginResId(R.dimen.atom_pubResDimenRecyclerViewDivider8dp)
                        .showLastDivider()
                        .build());

        maskerShowProgressView(true);
        getPresenter().postPayOrderNameList(listRequestParam.orderNo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payOrderRepeatPay:
                ListRequestParam param = (ListRequestParam) v.getTag();
                if (null != param) {
                    IClientActivityLaunchFactory.launchNameMasterActivity(this, param, 100, NameMasterAdapter.POSITION.FREEDOM);
                }
        }
    }

    @Override
    public void maskerOnClick(View view, int clickLabelRes) {
        super.maskerOnClick(view, clickLabelRes);
        maskerShowProgressView(true);
        getPresenter().postPayOrderNameList(listRequestParam.orderNo);
    }

    @Override
    public void postOnExplainError(int postId, Throwable throwable) {
        throwable.printStackTrace();
        switch (postId) {
            case IPost.Naming:
                if (throwable instanceof IllegalRequestException) {
                    maskerShowMaskerLayout(throwable.getMessage(), R.string.atom_pub_resStringRetry);
                } else {
                    maskerShowMaskerLayout(getString(R.string.atom_pub_resStringNetworkBroken), R.string.atom_pub_resStringRetry);
                }
                break ;
            default:
                maskerHideProgressView();
        }
    }

    @Override
    public void postOnNamingSuccess(RNameDefinition result) {
        maskerHideProgressView();
        if (null == result) {
            maskerShowMaskerLayout(getString(R.string.atom_pub_resStringNetworkBroken), 0);
            return ;
        } else if (result.size() == 0) {
            maskerShowMaskerLayout(getString(R.string.atom_pub_resStringNamingListLack), R.string.atom_pub_resStringRetry);
            return ;
        }

        List<NamingWordItem> itemList = new ArrayList<NamingWordItem>();
        int size = result.size();
        for (int i = 0; i < size; i ++) {
            itemList.add(new NamingWordItem(result.get(i)));
        }

        nameDefinitionAdapter.add(itemList);
        if (TextUtils.isEmpty(result.statusLabel)) {
            payOrderRepeatPay.setVisibility(View.GONE);
        } else {
            payOrderRepeatPay.setTag(new ListRequestParam(result));
            payOrderRepeatPay.setOnClickListener(this);
            payOrderRepeatPay.setVisibility(View.VISIBLE);
            payOrderRepeatPay.setText(result.statusLabel);
        }
    }

    @Override
    public void postOnFavoriteAddSuccess(RIdentifyResult result, Object item) {
        showToast(R.string.atom_pub_resStringFavoriteHintAdd);

        NamingWordItem namingWordItem = (NamingWordItem) item;
        namingWordItem.src.id = result.id;
        namingWordItem.src.favorite = true;
        nameDefinitionAdapter.notifyAdapterDataSetChanged();
    }

    @Override
    public void postOnFavoriteRemoveSuccess(Object item) {
        showToast(R.string.atom_pub_resStringFavoriteHintRemove);

        NamingWordItem namingWordItem = (NamingWordItem) item;
        namingWordItem.src.id = 0;
        namingWordItem.src.favorite = false;
        nameDefinitionAdapter.notifyAdapterDataSetChanged();
    }
}
