package rahul.com.spiderchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by rahul on 18/7/17.
 */

public class StartActivity extends Activity {

    private Button registerBtn;
    private Button signInBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_act_des);

        registerBtn = (Button) findViewById(R.id.reg_btn);
        signInBtn = (Button) findViewById(R.id.sign_btn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MyFilter","onclick event occurred");
                Intent intent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this,MyLogInActivity.class));
            }
        });
    }
}
