

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Activity.AdavanceListActivity;
import com.mv.Activity.ExpenseListActivity;
import com.mv.Activity.VoucherListActivity;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by acer on 9/8/2016.
 */


public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private List<Voucher> mDataList;
    private VoucherListActivity mActivity;
    private PopupMenu popup;
    private PreferenceHelper preferenceHelper;

    public VoucherAdapter(Context context, List<Voucher> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        mActivity = (VoucherListActivity) context;
        preferenceHelper = new PreferenceHelper(mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Voucher voucher = mDataList.get(position);
        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            holder.tvUser.setVisibility(View.VISIBLE);
            holder.tvUserName.setVisibility(View.VISIBLE);
            holder.tvUserName.setText(voucher.getUserName());
        } else {
            holder.tvUser.setVisibility(View.GONE);
            holder.tvUserName.setVisibility(View.GONE);
        }
        holder.tvProjectName.setText(voucher.getProject());
        holder.tvDateName.setText(voucher.getDate());
        holder.tvNoOfPeopleName.setText(voucher.getPlace());
        holder.tvTotalExpenseName.setText("₹ " + voucher.getApproved_Expense());
        holder.tvTotalAdvance.setText("₹ " + voucher.getApproved_Advance());

        holder.cardView.setOnClickListener(view -> {
            if (Constants.AccountTeamCode.equals("TeamManagement")) {
                mActivity.editVoucher(voucher);
            }
        });

        // hiding views for team mgmt section
        if (Constants.AccountTeamCode.equals("TeamManagement")) {
            holder.imgMore.setVisibility(View.GONE);
        }
        holder.imgMore.setOnClickListener(view -> {
            popup = new PopupMenu(mContext, holder.imgMore);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.popup_menu_vouchar, popup.getMenu());
            //   popup.getMenu().getItem(R.id.spam).setVisible(true);
            MenuItem edit = popup.getMenu().findItem(R.id.edit);
            MenuItem delete = popup.getMenu().findItem(R.id.delete);
            MenuItem sendMail = popup.getMenu().findItem(R.id.send_mail);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId()==R.id.edit) {
                    mActivity.editVoucher(voucher);
                } else if (item.getItemId()==R.id.delete) {
                    showLogoutPopUp(position);
                } else if (item.getItemId()==R.id.send_mail) {
                    sendEmail(voucher.getId());
                }
                return true;
            });
            popup.show();
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_voucher, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvUserName;
        TextView tvProjectName, tvDateName, tvNoOfPeopleName, tvTotalExpenseName, tvTotalAdvance;
        ImageView imgMore;
        LinearLayout layout_expense, layout_adavance;
        CardView cardView;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);

            imgMore = itemLayoutView.findViewById(R.id.img_more);

            cardView = itemLayoutView.findViewById(R.id.cardView);

            tvUser = itemLayoutView.findViewById(R.id.tvUser);
            tvUserName = itemLayoutView.findViewById(R.id.tvUserName);
            tvProjectName = itemLayoutView.findViewById(R.id.tvProjectName);
            tvDateName = itemLayoutView.findViewById(R.id.tvDateName);
            tvNoOfPeopleName = itemLayoutView.findViewById(R.id.tvNoOfPeopleName);
            tvTotalExpenseName = itemLayoutView.findViewById(R.id.tvTotalExpenseName);
            tvTotalAdvance = itemLayoutView.findViewById(R.id.tvTotalAdvance);
            layout_expense = itemLayoutView.findViewById(R.id.layout_expense);
            layout_adavance = itemLayoutView.findViewById(R.id.layout_adavance);

            layout_expense.setOnClickListener(view -> {
                Intent intent;
                intent = new Intent(mContext, ExpenseListActivity.class);
                intent.putExtra(Constants.VOUCHER, mDataList.get(getAdapterPosition()));
                mActivity.startActivity(intent);
            });
            layout_adavance.setOnClickListener(view -> {
                Intent intent;
                intent = new Intent(mContext, AdavanceListActivity.class);
                intent.putExtra(Constants.VOUCHER, mDataList.get(getAdapterPosition()));
                mActivity.startActivity(intent);
            });


        }
    }

    private void sendEmail(String id){
        if (Utills.isConnected(mContext)) {
            try {

                Utills.showProgressDialog(mContext);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("VoucherId", id);
                jsonObject.put("UserID", User.getCurrentUser(mContext).getMvUser().getId());
                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(mContext).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/sendPDFEmail", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                if (response.isSuccess()) {
                                    String data = response.body().string();
                                    if (data.length() > 0) {
                                        JSONObject object = new JSONObject(data);
                                        Utills.showToast(object.getString("Status"), mContext);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(mContext.getResources().getString(R.string.error_something_went_wrong), mContext.getApplicationContext());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(mContext.getResources().getString(R.string.error_something_went_wrong), mContext.getApplicationContext());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(mContext.getResources().getString(R.string.error_something_went_wrong), mContext.getApplicationContext());

            }
        } else {
            Utills.showToast(mContext.getResources().getString(R.string.error_no_internet), mContext.getApplicationContext());
        }

    }

    private void showLogoutPopUp(final int postion) {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.delete_task_string));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);

        // Setting CANCEL Button
        alertDialog.setButton2(mContext.getString(android.R.string.cancel), (dialog, which) -> {
            alertDialog.dismiss();
            // Write your code here to execute after dialog closed
          /*  listOfWrongQuestions.add(mPosition);
            prefObj.insertString( PreferenceHelper.WRONG_QUESTION_LIST_KEY_NAME, Utills.getStringFromList( listOfWrongQuestions ));*/
        });
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), (dialog, which) -> mActivity.deleteVoucher(postion));

        // Showing Alert Message
        alertDialog.show();
    }


}

