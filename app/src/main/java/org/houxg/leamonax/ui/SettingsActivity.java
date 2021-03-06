package org.houxg.leamonax.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.houxg.leamonax.BuildConfig;
import org.houxg.leamonax.R;
import org.houxg.leamonax.database.NoteDataStore;
import org.houxg.leamonax.database.NoteTagDataStore;
import org.houxg.leamonax.database.NotebookDataStore;
import org.houxg.leamonax.model.Account;
import org.houxg.leamonax.model.BaseResponse;
import org.houxg.leamonax.model.Tag;
import org.houxg.leamonax.service.AccountService;
import org.houxg.leamonax.service.NoteService;
import org.houxg.leamonax.utils.SharedPreferenceUtils;
import org.houxg.leamonax.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.houxg.leamonax.service.NoteService.SP_HAS_FTS_FULL_BUILD;

public class SettingsActivity extends BaseActivity {

    private final String[] mEditors = new String[]{"RichText", "Markdown"};

    @BindView(R.id.tv_editor)
    TextView mEditorTv;
    @BindView(R.id.tv_image_size)
    TextView mImageSizeTv;
    @BindView(R.id.iv_avatar)
    ImageView mAvatarIv;
    @BindView(R.id.tv_user_name)
    TextView mUserNameTv;
    @BindView(R.id.tv_email)
    TextView mEmailTv;
    @BindView(R.id.tv_host)
    TextView mHostTv;
    @BindView(R.id.ll_clear)
    View mClearDataView;
    @BindView(R.id.ll_fts_rebuild)
    View mFtsRebuildLayout;
    private ProgressDialog mDialog;
    @BindView(R.id.rotate_button_switch)
    SwitchCompat mRotateSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initToolBar((Toolbar) findViewById(R.id.toolbar), true);
        ButterKnife.bind(this);
        refresh();
        mRotateSwitch.setChecked(SharedPreferenceUtils.read(SharedPreferenceUtils.CONFIG, getString(R.string.rotate_button), false));
        mClearDataView.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.ll_editor)
    void selectEditor() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_editor)
                .setSingleChoiceItems(mEditors, Account.getCurrent().getDefaultEditor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Account account = Account.getCurrent();
                        account.setDefaultEditor(which);
                        account.update();
                        mEditorTv.setText(mEditors[which]);
                    }
                })
                .setCancelable(true)
                .show();
    }

    @OnClick(R.id.ll_log_out)
    void clickedLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.log_out)
                .setMessage(R.string.are_your_sure_to_log_out)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AccountService.logout();
                        List<Account> remainingAccount = AccountService.getAccountList();
                        if (remainingAccount.size() == 0) {
                            Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick(R.id.rl_avatar)
    void clickedAvatar() {

    }

    @OnClick(R.id.ll_user_name)
    void clickedUserName() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_sigle_edittext, null);
        final EditText mUserNameEt = (EditText) view.findViewById(R.id.edit);
        mUserNameEt.setText(Account.getCurrent().getUserName());
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_user_name)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String username = mUserNameEt.getText().toString();
                        mUserNameTv.setText(username);
                        changeUsername(username);
                    }
                })
                .show();
    }

    @OnClick(R.id.ll_change_password)
    void clickedPassword() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_passowrd, null);
        final EditText mOldPasswordEt = view.findViewById(R.id.et_old_password);
        final EditText mNewPasswordEt = view.findViewById(R.id.et_new_password);
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_password)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        changePassword(mOldPasswordEt.getText().toString(), mNewPasswordEt.getText().toString());
                    }
                })
                .show();
    }

    @OnClick(R.id.ll_clear)
    void clickedClearData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_data)
                .setMessage(R.string.are_you_sure_to_delete_all_data_in_this_account)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clearData();
                    }
                })
                .show();
    }

    private void showProgressDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(this);
            mDialog.setTitle(R.string.progress_dialog_loading_msg);
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @OnClick(R.id.ll_fts_rebuild)
    void rebuildIndex() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.full_text_search_index_rebuild)
                .setMessage(R.string.full_text_search_rebuild_index_msg)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();
                        SharedPreferenceUtils.write(SharedPreferenceUtils.CONFIG, SP_HAS_FTS_FULL_BUILD, false);
                        NoteService.buildFTSNote();
                        dialog.dismiss();
                        mFtsRebuildLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                ToastUtils.show(SettingsActivity.this, R.string.full_text_search_index_rebuild_success);
                            }
                        }, 1000 * 15);
                    }
                })
                .show();
    }

    @OnCheckedChanged(R.id.rotate_button_switch)
    void onCheckedChanged(SwitchCompat swit, boolean isChecked)
    {
        SharedPreferenceUtils.write(SharedPreferenceUtils.CONFIG, getString(R.string.rotate_button), isChecked);
    }


    private void clearData() {
        Observable.create(
                new Observable.OnSubscribe<Void>() {
                    @Override
                    public void call(Subscriber<? super Void> subscriber) {
                        if (!subscriber.isUnsubscribed()) {
                            Account currentUser = Account.getCurrent();
                            String userId = currentUser.getUserId();
                            NoteDataStore.deleteAll(userId);
                            NotebookDataStore.deleteAll(userId);
                            Tag.deleteAll(userId);
                            NoteTagDataStore.deleteAll(userId);
                            currentUser.setNoteUsn(0);
                            currentUser.setNotebookUsn(0);
                            currentUser.update();
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        ToastUtils.show(SettingsActivity.this, R.string.clear_data_successful);
                    }
                });
    }

    private void changeUsername(final String username) {
        AccountService.changeUserName(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showNetworkError(SettingsActivity.this);
                        mUserNameTv.setText(Account.getCurrent().getUserName());
                    }

                    @Override
                    public void onNext(BaseResponse baseResponse) {
                        if (baseResponse.isOk()) {
                            Account account = Account.getCurrent();
                            account.setUserName(username);
                            account.update();
                            ToastUtils.show(SettingsActivity.this, R.string.change_user_name_successful);
                        } else {
                            mUserNameTv.setText(Account.getCurrent().getUserName());
                            ToastUtils.show(SettingsActivity.this, R.string.change_user_name_failed);
                        }
                    }
                });
    }

    private void changePassword(String oldPassword, String newPassword) {
        AccountService.changePassword(oldPassword, newPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.showNetworkError(SettingsActivity.this);
                    }

                    @Override
                    public void onNext(BaseResponse baseResponse) {
                        if (!baseResponse.isOk()) {
                            ToastUtils.show(SettingsActivity.this, R.string.change_password_failed);
                        } else {
                            ToastUtils.show(SettingsActivity.this, R.string.change_password_successful);
                        }
                    }
                });
    }

    private void refresh() {
        Account current = Account.getCurrent();
        mEditorTv.setText(mEditors[current.getDefaultEditor()]);
        mUserNameTv.setText(current.getUserName());
        mEmailTv.setText(current.getEmail());
        mHostTv.setText(current.getHost());
        Glide.with(this)
                .load(current.getAvatar())
                .centerCrop()
                .into(mAvatarIv);
    }
}
