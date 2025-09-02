import React from 'react';

const Footer = () => {
  return (
    <footer className="h-[477px] flex flex-col justify-center items-center bg-[#0F172A] border-t-2 border-solid border-[#334155] text-white">
      {/* Logo and Branding */}
      <div className="flex items-center space-x-2">
        <div
          className="w-[21px] h-[24px]"
          style={{
            backgroundImage: 'url(https://picsum.photos/21/24)',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
        <span className="text-[17px] font-['Press_Start_2P'] leading-[25.5px]">
          Codédex
        </span>
        <img src="https://picsum.photos/id/25/24" alt="Logo" />
        <span className="text-[#CBD5E1] font-['Mulish'] font-extralight leading-6">
          Made with
        </span>
        <img src="https://picsum.photos/id/20/20" alt="Heart" />
        <span className="text-[#CBD5E1] font-['Mulish'] font-extralight leading-6">
          in Brooklyn, NY
        </span>
      </div>

      {/* Navigation Sections */}
      <div className="flex flex-row space-x-8 mt-8">
        {/* Company Section */}
        <div className="flex flex-col items-center">
          <span className="text-[#94A3B8] text-xs font-['Mulish'] font-extralight leading-[18px] tracking-[1.68px] mb-2">
            COMPANY
          </span>
          <div className="flex flex-col space-y-1">
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">About</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Blog</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Shop</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Community</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Help Center</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Pricing</a>
          </div>
        </div>

        {/* Practice Section */}
        <div className="flex flex-col items-center">
          <span className="text-[#94A3B8] text-xs font-['Mulish'] font-extralight leading-[18px] tracking-[1.68px] mb-2">
            PRACTICE
          </span>
          <div className="flex flex-col space-y-1">
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Challenges</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Projects</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">#30NitesOfCode</a>
          </div>
        </div>

        {/* Learn Section */}
        <div className="flex flex-col items-center">
          <span className="text-[#94A3B8] text-xs font-['Mulish'] font-extralight leading-[18px] tracking-[1.68px] mb-2">
            LEARN
          </span>
          <div className="flex flex-col space-y-1">
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">All Courses</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Python</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Intermediate Python</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">NumPy</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">SQL</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">HTML</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">CSS</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">JavaScript</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Intermediate JavaScript</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">React</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Command Line</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Git & GitHub</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">p5.js</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">C++</a>
            <a href="#" className="text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Java</a>
          </div>
        </div>
      </div>

      {/* Footer Bottom */}
      <div className="flex flex-col items-center mt-8">
        <div className="flex flex-row space-x-4">
          <span className="text-[#CBD5E1] text-sm font-['Mulish'] font-extralight leading-[21px]">
            © 2025 Niteowl, Inc.
          </span>
          <a href="#" className="text-[#CBD5E1] text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Terms</a>
          <a href="#" className="text-[#CBD5E1] text-sm font-['Mulish'] font-extralight leading-[21px] hover:underline">Privacy Policy</a>
        </div>
        <div className="flex flex-row space-x-4 mt-4">
          {[
            'https://picsum.photos/id/32/32',
            'https://picsum.photos/id/32/32',
            'https://picsum.photos/id/32/32',
            'https://picsum.photos/id/32/32',
            'https://picsum.photos/id/32/32',
            'https://picsum.photos/32/32',
            'https://picsum.photos/34/34',
            'https://picsum.photos/42/42',
          ].map((src, index) => (
            <div key={index} className="w-[32px] h-[32px]">
              <img
                src={src}
                alt={`Social Icon ${index + 1}`}
                className="w-full h-full object-cover"
              />
            </div>
          ))}
        </div>
      </div>
    </footer>
  );
};

export default Footer;