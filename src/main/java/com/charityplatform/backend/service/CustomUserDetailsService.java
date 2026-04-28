package com.charityplatform.backend.service;




import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;

    }
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user=userRepository.findByUsername(usernameOrEmail)
                .orElseGet(()->
                        userRepository.findByEmail(usernameOrEmail)
                                .orElseThrow(()->
                        new UsernameNotFoundException("Aint got no user with that username or email: "+usernameOrEmail )
                ));
return user;
    }
@Transactional(readOnly = true)
public UserDetails loadUserByUserId(Long id) throws UsernameNotFoundException {
        User user=userRepository.findById(id).orElseThrow(
            ()->new UsernameNotFoundException("User now found with id: "+ id)
    );
        return user;
}

}
