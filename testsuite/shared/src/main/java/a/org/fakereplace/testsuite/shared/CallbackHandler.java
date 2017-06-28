/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package a.org.fakereplace.testsuite.shared;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

/**
 * @author Stuart Douglas
 */
public class CallbackHandler implements javax.security.auth.callback.CallbackHandler{
    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(final Callback current : callbacks) {
            if(current instanceof NameCallback) {
                ((NameCallback) current).setName("$local");
            } else if(current instanceof RealmCallback) {
                ((RealmCallback) current).setText(((RealmCallback) current).getDefaultText());
            }
        }

    }
}
