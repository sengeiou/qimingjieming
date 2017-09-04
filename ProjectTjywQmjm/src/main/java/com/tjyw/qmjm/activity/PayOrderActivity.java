package com.tjyw.qmjm.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.swiftfintech.pay.MainApplication;
import com.swiftfintech.pay.activity.PayPlugin;
import com.swiftfintech.pay.bean.RequestMsg;
import com.swiftfintech.pay.handle.PayHandlerManager;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tjyw.atom.alipay.AlipayResult;
import com.tjyw.atom.alipay.IAlipayCallback;
import com.tjyw.atom.alipay.PayAlipayBuilder;
import com.tjyw.atom.network.IllegalRequestException;
import com.tjyw.atom.network.RxSchedulersHelper;
import com.tjyw.atom.network.conf.IApiField;
import com.tjyw.atom.network.conf.ICode;
import com.tjyw.atom.network.model.PayOrder;
import com.tjyw.atom.network.model.PayService;
import com.tjyw.atom.network.param.ListRequestParam;
import com.tjyw.atom.network.presenter.PayPresenter;
import com.tjyw.atom.network.presenter.listener.OnApiPayPostListener;
import com.tjyw.atom.network.presenter.listener.OnApiPostErrorListener;
import com.tjyw.atom.network.result.RetroPayPreviewResult;
import com.tjyw.atom.pub.inject.From;
import com.tjyw.qmjm.R;

import java.lang.ref.WeakReference;

import nucleus.factory.RequiresPresenter;
import rx.functions.Action1;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by stephen on 17-8-17.
 */
@RequiresPresenter(PayPresenter.class)
public class PayOrderActivity extends BaseToolbarActivity<PayPresenter<PayOrderActivity>>
        implements OnApiPayPostListener.PostPayOrderListener, OnApiPayPostListener.PostPayPreviewListener, OnApiPostErrorListener, IAlipayCallback {

    @From(R.id.payServiceName)
    protected TextView payServiceName;

    @From(R.id.payServicePrice)
    protected TextView payServicePrice;

    @From(R.id.payUseAlipay)
    protected TextView payUseAlipay;

    @From(R.id.payUseWxPay)
    protected TextView payUseWxPay;

    @From(R.id.atom_pub_resIdsOK)
    protected TextView atom_pub_resIdsOK;

    protected PayOrderHandler payOrderHandler;

    protected ListRequestParam param;

    protected PayService payService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        param = (ListRequestParam) pGetSerializableExtra(IApiField.P.param);
        payService = (PayService) pGetSerializableExtra(IApiField.P.payService);
        if (null == param || null == payService) {
            finish();
            return ;
        } else {
            setContentView(R.layout.atom_pay_order);
            tSetToolBar(getString(R.string.atom_pub_resStringPayOrder));

            immersionBarWith()
                    .fitsSystemWindows(true)
                    .statusBarColor(R.color.colorPrimary)
                    .statusBarDarkFont(true)
                    .init();
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.atom_pub_resStringPayPrice));
        int length = builder.length();
        builder.append(getString(R.string.atom_pub_resStringRMB_s, payService.money));
        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.atom_pub_resTextColorRed)), length, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        payServicePrice.setText(builder);
        payServiceName.setText(getString(R.string.atom_pub_resStringPayService, payService.service));

        payUseAlipay.setSelected(true);
        payUseAlipay.setOnClickListener(this);
        payUseWxPay.setOnClickListener(this);
        atom_pub_resIdsOK.setOnClickListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Intent result = new Intent();
//        data.putExtra(IApiField.O.orderNo, orderNo);
//        setResult(ICode.PAY.ALIPAY_SUCCESS, result);
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != payOrderHandler) {
            payOrderHandler.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payUseAlipay:
                v.setSelected(true);
                payUseWxPay.setSelected(false);
                break ;
            case R.id.payUseWxPay:
                v.setSelected(true);
                payUseAlipay.setSelected(false);
                break ;
            case R.id.atom_pub_resIdsOK:
                RxPermissions permissions = new RxPermissions(this);
                if (permissions.isGranted(Manifest.permission.READ_PHONE_STATE)) {
                    doPostPay();
                } else {
                    permissions
                            .request(Manifest.permission.READ_PHONE_STATE)
                            .compose(RxSchedulersHelper.<Boolean>io_main())
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean granted) {
                                    if (granted) {
                                        doPostPay();
                                    } else {
                                        showToast(R.string.atom_pub_resStringPermissionByUserDeny);
                                    }
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    showToast(R.string.atom_pub_resStringPermissionByUserDeny);
                                }
                            });
                }
        }
    }

    protected void doPostPay() {
        if (payUseAlipay.isSelected()) {
            maskerShowProgressView(true);
            getPresenter().postPayPreview(
                    1,
                    param.surname,
                    param.day,
                    param.gender,
                    param.nameNumber
            );
        } else if (payUseWxPay.isSelected()) {
            maskerShowProgressView(true);
            getPresenter().postPayOrder(
                    1,
                    param.surname,
                    param.day,
                    param.gender,
                    param.nameNumber
            );
        }
    }

    @Override
    public void postOnPayOrderSuccess(PayOrder payOrder) {
        if (null == payOrderHandler) {
            payOrderHandler = new PayOrderHandler(PayOrderActivity.this);
        }

        PayHandlerManager.registerHandler(PayHandlerManager.PAY_H5_RESULT, payOrderHandler);
        maskerHideProgressView();

        RequestMsg msg = new RequestMsg();
        msg.setTokenId(payOrder.token_id);
        msg.setTradeType(MainApplication.PAY_WX_WAP);
        PayPlugin.unifiedH5Pay(this, msg);
    }

    @Override
    public void postOnPayPreviewSuccess(RetroPayPreviewResult result) {
        PayAlipayBuilder.getInstance().build(this, result, this);
        maskerHideProgressView();
    }

    @Override
    public void postOnExplainError(int postId, Throwable throwable) {
        maskerHideProgressView();
        if (throwable instanceof IllegalRequestException) {
            showToast(throwable.getMessage());
        } else {
            throwable.printStackTrace();
            showToast(R.string.atom_pub_resStringNetworkBroken);
        }
    }

    @Override
    public void pOnAliPayCallback(int resultStatus, AlipayResult result, String orderNo) {
        switch (resultStatus) {
            case RESULT_STATUS.SUCCESS:
                Intent data = new Intent();
                data.putExtra(IApiField.O.orderNo, orderNo);
                setResult(ICode.PAY.ALIPAY_SUCCESS, data);
                finish();
                break ;
            case RESULT_STATUS.FAIL:
            default:

        }
    }

    public static class PayOrderHandler extends Handler {

        protected WeakReference<PayOrderActivity> context;

        public PayOrderHandler(PayOrderActivity payOrderActivity) {
            this.context = new WeakReference<PayOrderActivity>(payOrderActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            PayOrderActivity payOrderActivity = context.get();
            if (null != payOrderActivity && ! payOrderActivity.isFinishing()) {
                switch (msg.what) {
                    case PayHandlerManager.PAY_H5_FAILED:
                        payOrderActivity.showToast(String.valueOf(msg.obj));
                }
            } else {
                clear();
            }
        }

        public void clear() {
            removeCallbacksAndMessages(null);
            if (null != context) {
                context.clear();
                context = null;
            }
        }
    }
}
